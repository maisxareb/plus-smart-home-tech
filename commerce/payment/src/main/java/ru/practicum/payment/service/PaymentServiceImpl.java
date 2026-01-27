package ru.practicum.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.interaction.api.order.client.OrderClient;
import ru.practicum.interaction.api.order.dto.OrderDto;
import ru.practicum.interaction.api.payment.dto.PaymentDto;
import ru.practicum.interaction.api.payment.dto.PaymentStatus;
import ru.practicum.interaction.api.shopping.store.client.ShoppingStoreClient;
import ru.practicum.interaction.api.shopping.store.dto.ProductDto;
import ru.practicum.interaction.api.shopping.store.exception.ProductNotFoundException;
import ru.practicum.payment.exception.PaymentNotFound;
import ru.practicum.payment.model.Payment;
import ru.practicum.payment.mapper.PaymentMapper;
import ru.practicum.payment.repository.PaymentRepository;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository repository;
    private final PaymentMapper paymentMapper;

    private final ShoppingStoreClient shoppingStoreClient;
    private final OrderClient orderClient;

    private final BigDecimal TAX = BigDecimal.valueOf(0.1);

    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    @Override
    @Transactional
    public PaymentDto goToPayment(OrderDto order) {
        Payment newPayment = Payment.builder()
                .totalProduct(order.getProductPrice())
                .deliveryTotal(order.getDeliveryPrice())
                .totalPayment(order.getTotalPrice())
                .feeTotal(order.getTotalPrice()
                        .subtract(order.getDeliveryPrice())
                        .subtract(order.getProductPrice()))
                .build();

        Payment savedPayment = repository.save(newPayment);
        return paymentMapper.toDto(savedPayment);
    }

    @Override
    public BigDecimal calculateTotalCost(OrderDto order) {
        BigDecimal productCostWithTax = order.getProductPrice()
                .add(order.getProductPrice().multiply(TAX));

        return order.getDeliveryPrice().add(productCostWithTax);
    }

    @Override
    @Transactional
    public void createRefund(UUID paymentId) {
        Payment payment = getPayment(paymentId);

        payment.setStatus(PaymentStatus.SUCCESS);
        Payment savedPayment = repository.save(payment);

        OrderDto order = orderClient.getOrderByPayment(paymentId);
        orderClient.paymentOrder(order.getOrderId());

        log.info("Оплата с id {} прошла успешно!", paymentId);
    }

    @Override
    public BigDecimal calculateProductCost(OrderDto order) {
        Map<UUID, Integer> products = order.getProducts();
        if (products == null || products.isEmpty()) {
            return BigDecimal.ZERO;
        }

        List<UUID> productIds = new ArrayList<>(products.keySet());

        try {
            return calculateWithBatchRequest(products, productIds);
        } catch (Exception e) {
            log.warn("Batch запрос не поддерживается, используем параллельные запросы");
            return calculateWithParallelRequests(products, productIds);
        }
    }

    private BigDecimal calculateWithBatchRequest(Map<UUID, Integer> products, List<UUID> productIds) {
        try {
            List<ProductDto> allProducts = shoppingStoreClient.getProductsByIds(productIds);

            Map<UUID, ProductDto> productMap = allProducts.stream()
                    .filter(product -> product != null && product.getProductId() != null)
                    .collect(Collectors.toMap(
                            ProductDto::getProductId,
                            product -> product,
                            (existing, replacement) -> existing
                    ));

            BigDecimal totalCost = BigDecimal.ZERO;

            for (Map.Entry<UUID, Integer> entry : products.entrySet()) {
                UUID productId = entry.getKey();
                Integer quantity = entry.getValue();

                ProductDto product = productMap.get(productId);
                if (product != null && product.getPrice() != null) {
                    totalCost = totalCost.add(
                            product.getPrice().multiply(BigDecimal.valueOf(quantity))
                    );
                } else {
                    log.warn("Продукт с id: {} не найден или не имеет цены, пропускаю.", productId);
                }
            }

            return totalCost;

        } catch (Exception e) {
            log.error("Ошибка при batch запросе продуктов: {}", e.getMessage());
            return calculateWithSequentialRequests(products);
        }
    }

    private BigDecimal calculateWithParallelRequests(Map<UUID, Integer> products, List<UUID> productIds) {
        List<CompletableFuture<BigDecimal>> futures = productIds.stream()
                .map(productId -> CompletableFuture.supplyAsync(() -> {
                    try {
                        ProductDto product = shoppingStoreClient.getProductById(productId);
                        Integer quantity = products.get(productId);
                        if (product != null && product.getPrice() != null && quantity != null) {
                            return product.getPrice().multiply(BigDecimal.valueOf(quantity));
                        }
                    } catch (ProductNotFoundException e) {
                        log.warn("Продукт с id: {} не найден, пропускаю.", productId);
                    } catch (Exception e) {
                        log.error("Ошибка при запросе продукта {}: {}", productId, e.getMessage());
                    }
                    return BigDecimal.ZERO;
                }, executorService))
                .collect(Collectors.toList());

        return futures.stream()
                .map(CompletableFuture::join)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateWithSequentialRequests(Map<UUID, Integer> products) {
        BigDecimal productCost = BigDecimal.ZERO;

        for (Map.Entry<UUID, Integer> entry : products.entrySet()) {
            UUID productId = entry.getKey();
            Integer quantity = entry.getValue();

            try {
                ProductDto product = shoppingStoreClient.getProductById(productId);
                if (product != null && product.getPrice() != null) {
                    productCost = productCost.add(
                            product.getPrice().multiply(BigDecimal.valueOf(quantity))
                    );
                }
            } catch (ProductNotFoundException e) {
                log.warn("Продукт с id: {} не найден, пропускаю.", productId);
            } catch (Exception e) {
                log.error("Ошибка при запросе продукта {}: {}", productId, e.getMessage());
            }
        }

        return productCost;
    }

    @Override
    @Transactional
    public void failedPayment(UUID paymentId) {
        Payment payment = getPayment(paymentId);

        payment.setStatus(PaymentStatus.FAILED);
        repository.save(payment);

        OrderDto order = orderClient.getOrderByPayment(paymentId);
        orderClient.failedPaymentOrder(order.getOrderId());

        log.warn("Ошибка при оплате с id {}!", paymentId);
    }

    private Payment getPayment(UUID paymentId) {
        return repository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFound("Оплата с id " + paymentId + " не найдена!"));
    }
}
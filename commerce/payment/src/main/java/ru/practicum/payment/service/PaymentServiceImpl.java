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
import java.util.Map;
import java.util.UUID;

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
        BigDecimal productCost = BigDecimal.ZERO;

        for (Map.Entry<UUID, Integer> entry : products.entrySet()) {
            UUID productId = entry.getKey();
            Integer quantity = entry.getValue();

            try {
                ProductDto product = shoppingStoreClient.getProductById(productId);
                productCost = productCost.add(
                        product.getPrice().multiply(BigDecimal.valueOf(quantity))
                );
            } catch (ProductNotFoundException e) {
                log.warn("Продукт с id: {} не найден, пропускаю.", productId);
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
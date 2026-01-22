package ru.practicum.order.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.interaction.api.delivery.client.DeliveryClient;
import ru.practicum.interaction.api.delivery.dto.DeliveryDto;
import ru.practicum.interaction.api.order.dto.OrderDto;
import ru.practicum.interaction.api.order.dto.OrderState;
import ru.practicum.interaction.api.order.exception.NotAuthorizedUserException;
import ru.practicum.interaction.api.payment.client.PaymentClient;
import ru.practicum.interaction.api.payment.dto.PaymentDto;
import ru.practicum.interaction.api.warehouse.exception.ProductLowQuantityInWarehouse;
import ru.practicum.interaction.api.warehouse.client.WarehouseClient;
import ru.practicum.interaction.api.warehouse.dto.AssemblyProductsForOrderRequest;
import ru.practicum.interaction.api.warehouse.dto.BookedProductsDto;
import ru.practicum.order.exception.NoOrderFoundException;
import ru.practicum.order.exception.NoSpecifiedProductInWarehouseException;
import ru.practicum.order.model.ProductReturnRequest;
import ru.practicum.order.model.CreateNewOrderRequest;
import ru.practicum.order.model.Order;
import ru.practicum.order.mapper.OrderMapper;
import ru.practicum.order.repository.OrderRepository;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository repository;
    private final OrderMapper orderMapper;

    private final DeliveryClient deliveryClient;
    private final PaymentClient paymentClient;
    private final WarehouseClient warehouseClient;

    @Override
    public Page<OrderDto> getOrder(String username, Pageable pageable) {
        if (username == null) {
            throw new NotAuthorizedUserException("Поле username не может быть пустым!");
        }

        Page<Order> orders = repository.findByUsername(username, pageable);
        return orders.map(orderMapper::toDto);
    }

    @Override
    public OrderDto getOrderByPayment(UUID paymentId) {
        Order order = repository.findByPaymentId(paymentId)
                .orElseThrow(() -> new NoOrderFoundException("Заказ с id оплаты " + paymentId + " не найден!"));
        return orderMapper.toDto(order);
    }

    @Override
    public OrderDto getOrderByDelivery(UUID deliveryId) {
        Order order = repository.findByDeliveryId(deliveryId)
                .orElseThrow(() -> new NoOrderFoundException("Заказ с id доставки " + deliveryId + " не найден!"));
        return orderMapper.toDto(order);
    }

    @Override
    @Transactional
    public OrderDto createOrder(String username, CreateNewOrderRequest request) {
        if (username == null) {
            throw new NotAuthorizedUserException("Поле username не может быть пустым!");
        }

        Order newOrder = Order.builder()
                .shoppingCartId(request.getShoppingCart().getShoppingCartId())
                .products(request.getShoppingCart().getProducts())
                .username(username)
                .build();

        Order order = saveOrder(newOrder);

        try {
            BookedProductsDto bookedProducts = warehouseClient.assemblyProducts(AssemblyProductsForOrderRequest.builder()
                    .products(request.getShoppingCart().getProducts())
                    .orderId(order.getOrderId())
                    .build());

            order.setDeliveryWeight(bookedProducts.getDeliveryWeight());
            order.setDeliveryVolume(bookedProducts.getDeliveryVolume());
            order.setFragile(bookedProducts.getFragile());

            OrderDto orderDto = orderMapper.toDto(order);
            order.setProductPrice(paymentClient.calculateProductCost(orderDto));

            DeliveryDto delivery = deliveryClient.createDelivery(
                    DeliveryDto.builder()
                            .fromAddress(warehouseClient.getWarehouseAddress())
                            .toAddress(request.getDeliveryAddress())
                            .orderId(order.getOrderId())
                            .build()
            );
            order.setDeliveryId(delivery.getDeliveryId());

            BigDecimal deliveryPrice = deliveryClient.calculateDeliveryCost(orderDto);
            order.setDeliveryPrice(deliveryPrice);

            BigDecimal totalPrice = paymentClient.calculateTotalCost(orderDto);
            order.setTotalPrice(totalPrice);

            PaymentDto payment = paymentClient.goToPayment(orderDto);
            order.setPaymentId(payment.getPaymentId());

            repository.save(order);

            paymentClient.refund(payment.getPaymentId());

            Order finalOrder = getOrder(order.getOrderId());
            return orderMapper.toDto(finalOrder);
        } catch (ProductLowQuantityInWarehouse e) {
            repository.delete(order);
            throw new ProductLowQuantityInWarehouse(e.getMessage());
        } catch (NoSpecifiedProductInWarehouseException e){
            repository.delete(order);
            throw new NoSpecifiedProductInWarehouseException(e.getMessage());
        } catch (Exception e) {
            repository.delete(order);
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    @Transactional
    public OrderDto returnOrder(ProductReturnRequest request) {
        Order order = getOrder(request.getOrderId());

        warehouseClient.returnProducts(request.getProducts());
        order.setState(OrderState.PRODUCT_RETURNED);

        Order savedOrder = repository.save(order);
        return orderMapper.toDto(savedOrder);
    }

    @Override
    @Transactional
    public OrderDto paymentOrder(UUID orderId) {
        Order order = getOrder(orderId);
        order.setState(OrderState.PAID);

        Order savedOrder = repository.save(order);
        return orderMapper.toDto(savedOrder);
    }

    @Override
    @Transactional
    public OrderDto failedPaymentOrder(UUID orderId) {
        Order order = getOrder(orderId);
        order.setState(OrderState.PAYMENT_FAILED);

        Order savedOrder = repository.save(order);
        return orderMapper.toDto(savedOrder);
    }

    @Override
    @Transactional
    public OrderDto deliveryOrder(UUID orderId) {
        Order order = getOrder(orderId);
        order.setState(OrderState.DELIVERED);

        Order savedOrder = repository.save(order);
        return orderMapper.toDto(savedOrder);
    }

    @Override
    @Transactional
    public OrderDto failedDeliveryOrder(UUID orderId) {
        Order order = getOrder(orderId);
        order.setState(OrderState.DELIVERY_FAILED);

        Order savedOrder = repository.save(order);
        return orderMapper.toDto(savedOrder);
    }

    @Override
    @Transactional
    public OrderDto completedOrder(UUID orderId) {
        Order order = getOrder(orderId);
        order.setState(OrderState.COMPLETED);

        Order savedOrder = repository.save(order);
        return orderMapper.toDto(savedOrder);
    }

    @Override
    @Transactional
    public OrderDto calculateTotalOrder(UUID orderId) {
        Order order = getOrder(orderId);
        OrderDto orderDto = orderMapper.toDto(order);
        order.setTotalPrice(paymentClient.calculateTotalCost(orderDto));

        Order savedOrder = repository.save(order);
        return orderMapper.toDto(savedOrder);
    }

    @Override
    @Transactional
    public OrderDto calculateDeliveryOrder(UUID orderId) {
        Order order = getOrder(orderId);
        OrderDto orderDto = orderMapper.toDto(order);
        order.setDeliveryPrice(deliveryClient.calculateDeliveryCost(orderDto));

        Order savedOrder = repository.save(order);
        return orderMapper.toDto(savedOrder);
    }

    @Override
    @Transactional
    public OrderDto assemblyOrder(UUID orderId) {
        Order order = getOrder(orderId);
        order.setState(OrderState.ASSEMBLED);

        Order savedOrder = repository.save(order);
        return orderMapper.toDto(savedOrder);
    }

    @Override
    @Transactional
    public OrderDto failedAssemblyOrder(UUID orderId) {
        Order order = getOrder(orderId);
        order.setState(OrderState.ASSEMBLY_FAILED);

        Order savedOrder = repository.save(order);
        return orderMapper.toDto(savedOrder);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Order saveOrder(Order newOrder) {
        return repository.save(newOrder);
    }

    private Order getOrder(UUID orderId) {
        return repository.findById(orderId)
                .orElseThrow(() -> new NoOrderFoundException("Заказ с id " + orderId + " не найден!"));
    }
}
package ru.practicum.delivery.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.delivery.exception.NoDeliveryFoundException;
import ru.practicum.delivery.model.Delivery;
import ru.practicum.delivery.model.DeliveryAddress;
import ru.practicum.delivery.mapper.DeliveryMapper;
import ru.practicum.delivery.repository.DeliveryRepository;
import ru.practicum.interaction.api.delivery.dto.DeliveryDto;
import ru.practicum.interaction.api.delivery.dto.DeliveryState;
import ru.practicum.interaction.api.order.client.OrderClient;
import ru.practicum.interaction.api.order.dto.OrderDto;
import ru.practicum.interaction.api.warehouse.client.WarehouseClient;
import ru.practicum.interaction.api.warehouse.dto.ShippedToDeliveryRequest;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryRepository repository;
    private final DeliveryMapper deliveryMapper;

    private final OrderClient orderClient;
    private final WarehouseClient warehouseClient;

    private final BigDecimal BASE_DELIVERY_COST = BigDecimal.valueOf(5.0);
    private final BigDecimal FRAGILE_RATIO = BigDecimal.valueOf(0.2);
    private final BigDecimal WEIGHT_RATIO = BigDecimal.valueOf(0.3);
    private final BigDecimal VOLUME_RATIO = BigDecimal.valueOf(0.2);
    private final BigDecimal ADDRESS_RATIO = BigDecimal.valueOf(0.2);

    @Override
    @Transactional
    public DeliveryDto createDelivery(DeliveryDto deliveryDto) {
        Delivery delivery = deliveryMapper.toEntity(deliveryDto);
        return deliveryMapper.toDto(repository.save(delivery));
    }

    @Override
    @Transactional
    public void successfulDelivery(UUID deliveryId) {
        Delivery delivery = getDelivery(deliveryId);

        delivery.setDeliveryState(DeliveryState.DELIVERED);
        repository.save(delivery);

        OrderDto order = orderClient.getOrderByDelivery(deliveryId);
        orderClient.deliveryOrder(order.getOrderId());

        log.info("Заказ с id: {} успешно доставлен!", order.getOrderId());
    }

    @Override
    @Transactional
    public void pickedDelivery(UUID deliveryId) {
        Delivery delivery = getDelivery(deliveryId);

        OrderDto order = orderClient.getOrderByDelivery(deliveryId);
        orderClient.assemblyOrder(order.getOrderId());

        warehouseClient.shippedOrder(ShippedToDeliveryRequest.builder()
                .deliveryId(deliveryId)
                .orderId(order.getOrderId())
                .build());

        delivery.setDeliveryState(DeliveryState.IN_PROGRESS);
        repository.save(delivery);

        log.info("Товар для доставки с id: {} успешно получен!", deliveryId);
    }

    @Override
    @Transactional
    public void failedDelivery(UUID deliveryId) {
        Delivery delivery = getDelivery(deliveryId);

        delivery.setDeliveryState(DeliveryState.CANCELLED);
        repository.save(delivery);

        OrderDto order = orderClient.getOrderByDelivery(deliveryId);
        orderClient.failedDeliveryOrder(order.getOrderId());

        log.warn("Неудачно вручен товар из доставки с id: {} !", deliveryId);
    }

    @Override
    public BigDecimal calculateDeliveryCost(OrderDto order) {
        Delivery delivery = getDelivery(order.getDeliveryId());
        return calculateDelivery(delivery, order);
    }

    private Delivery getDelivery(UUID deliveryId) {
        return repository.findById(deliveryId)
                .orElseThrow(() -> new NoDeliveryFoundException("Доставка с id: " + deliveryId + " не найдена!"));
    }

    private boolean isAddressContains(DeliveryAddress address, String substring) {
        if (address == null || substring == null) return false;

        return (address.getCountry().contains(substring)
                || address.getCity().contains(substring)
                || address.getStreet().contains(substring)
                || address.getHouse().contains(substring)
                || address.getFlat().contains(substring));
    }

    private BigDecimal calculateDelivery(Delivery delivery, OrderDto order) {
        BigDecimal deliveryPrice = BASE_DELIVERY_COST;

        if (isAddressContains(delivery.getFromAddress(), "ADDRESS_1")) {
            deliveryPrice = deliveryPrice.multiply(BigDecimal.valueOf(1)).add(BASE_DELIVERY_COST);
        }
        else if (isAddressContains(delivery.getFromAddress(), "ADDRESS_2")) {
            deliveryPrice = deliveryPrice.multiply(BigDecimal.valueOf(2)).add(BASE_DELIVERY_COST);
        }

        if (order.getFragile()) {
            deliveryPrice = deliveryPrice.add(deliveryPrice.multiply(FRAGILE_RATIO));
        }

        deliveryPrice = deliveryPrice.add(BigDecimal.valueOf(order.getDeliveryWeight()).multiply(WEIGHT_RATIO));
        deliveryPrice = deliveryPrice.add(BigDecimal.valueOf(order.getDeliveryVolume()).multiply(VOLUME_RATIO));

        if (!delivery.getFromAddress().getStreet().equals(delivery.getToAddress().getStreet())) {
            deliveryPrice = deliveryPrice.add(deliveryPrice.multiply(ADDRESS_RATIO));
        }

        return deliveryPrice;
    }
}
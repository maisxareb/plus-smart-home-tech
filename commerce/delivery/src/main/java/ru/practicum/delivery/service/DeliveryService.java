package ru.practicum.delivery.service;

import ru.practicum.interaction.api.delivery.dto.DeliveryDto;
import ru.practicum.interaction.api.order.dto.OrderDto;

import java.math.BigDecimal;
import java.util.UUID;

public interface DeliveryService {

    DeliveryDto createDelivery(DeliveryDto delivery);

    void successfulDelivery(UUID deliveryId);

    void pickedDelivery(UUID deliveryId);

    void failedDelivery(UUID deliveryId);

    BigDecimal calculateDeliveryCost(OrderDto order);
}
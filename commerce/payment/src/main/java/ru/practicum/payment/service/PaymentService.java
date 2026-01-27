package ru.practicum.payment.service;

import ru.practicum.interaction.api.order.dto.OrderDto;
import ru.practicum.interaction.api.payment.dto.PaymentDto;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentService {

    PaymentDto goToPayment(OrderDto order);

    BigDecimal calculateTotalCost(OrderDto order);

    void createRefund(UUID paymentId);

    BigDecimal calculateProductCost(OrderDto order);

    void failedPayment(UUID paymentId);
}
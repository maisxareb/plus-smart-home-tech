package ru.practicum.interaction.api.payment.client;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.practicum.interaction.api.order.dto.OrderDto;
import ru.practicum.interaction.api.payment.dto.PaymentDto;

import java.math.BigDecimal;
import java.util.UUID;

@FeignClient(name = "payment")
public interface PaymentClient {

    @PostMapping("/api/v1/payment")
    PaymentDto goToPayment(@RequestBody @Valid OrderDto order);

    @PostMapping("/api/v1/payment/refund")
    void refund(@RequestBody UUID paymentId);

    @PostMapping("/api/v1/payment/totalCost")
    BigDecimal calculateTotalCost(@RequestBody @Valid OrderDto order);

    @PostMapping("/api/v1/payment/productCost")
    BigDecimal calculateProductCost(@RequestBody @Valid OrderDto order);
}
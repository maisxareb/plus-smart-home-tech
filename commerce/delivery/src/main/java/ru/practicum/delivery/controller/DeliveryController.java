package ru.practicum.delivery.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.delivery.service.DeliveryService;
import ru.practicum.interaction.api.delivery.dto.DeliveryDto;
import ru.practicum.interaction.api.order.dto.OrderDto;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/delivery")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService service;

    @PutMapping
    public DeliveryDto createDelivery(@RequestBody @Valid DeliveryDto delivery) {
        return service.createDelivery(delivery);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/successful")
    public void successfulDelivery(@RequestBody UUID deliveryId) {
        service.successfulDelivery(deliveryId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/picked")
    public void pickedDelivery(@RequestBody UUID deliveryId) {
        service.pickedDelivery(deliveryId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/failed")
    public void failedDelivery(@RequestBody UUID deliveryId) {
        service.failedDelivery(deliveryId);
    }

    @PostMapping("/cost")
    public BigDecimal calculateDeliveryCost(@RequestBody @Valid OrderDto order) {
        return service.calculateDeliveryCost(order);
    }
}
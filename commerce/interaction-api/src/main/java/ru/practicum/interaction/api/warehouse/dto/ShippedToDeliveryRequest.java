package ru.practicum.interaction.api.warehouse.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ShippedToDeliveryRequest {

    @NotNull
    private UUID orderId;

    @NotNull
    private UUID deliveryId;
}
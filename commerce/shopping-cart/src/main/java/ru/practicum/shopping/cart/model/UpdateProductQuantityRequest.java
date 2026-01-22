package ru.practicum.shopping.cart.model;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class UpdateProductQuantityRequest {

    @NotNull
    private UUID productId;

    @NotNull
    private Integer newQuantity;
}
package ru.practicum.shopping_cart.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateProductQuantityRequest {

    @NotBlank
    private String productId;

    @NotNull
    private Integer newQuantity;
}
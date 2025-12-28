package ru.practicum.shopping.store.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateStockLevelRequest {

    @NotBlank
    private String productId;

    @NotNull
    private QuantityState quantityState;
}
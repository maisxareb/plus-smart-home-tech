package ru.practicum.shopping.store.model;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class UpdateStockLevelRequest {

    @NotNull
    private UUID productId;

    @NotNull
    private QuantityState quantityState;
}
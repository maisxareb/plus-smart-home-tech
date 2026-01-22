package ru.practicum.warehouse.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import ru.practicum.interaction.api.warehouse.dto.DimensionDto;

import java.util.UUID;

@Data
@Builder
public class NewProductInWarehouseRequest {

    @NotNull
    private UUID productId;

    @NotNull
    private Boolean fragile;

    @NotNull
    private DimensionDto dimension;

    @NotNull
    @Min(1)
    private double weight;
}
package ru.practicum.interaction_api.warehouse.dto;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Embeddable
public class DimensionDto {
    @NotNull
    private Double width;

    @NotNull
    private Double height;

    @NotNull
    private Double depth;
}
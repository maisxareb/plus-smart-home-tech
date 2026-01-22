package ru.practicum.interaction.api.shopping.store.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SortDto {
    private String property;
    private String direction;
}
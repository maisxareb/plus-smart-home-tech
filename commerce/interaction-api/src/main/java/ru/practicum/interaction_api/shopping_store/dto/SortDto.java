package ru.practicum.interaction_api.shopping_store.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SortDto {
    private String property;
    private String direction;
}
package ru.practicum.interaction.api.shopping.cart.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class ShoppingCartDto {

    @NotNull
    private UUID shoppingCartId;

    @NotNull
    private Map<UUID, Integer> products;
}
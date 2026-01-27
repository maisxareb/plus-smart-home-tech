package ru.practicum.order.model;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import ru.practicum.interaction.api.shopping.cart.dto.ShoppingCartDto;
import ru.practicum.interaction.api.warehouse.dto.AddressDto;

@Data
@Builder
public class CreateNewOrderRequest {

    @NotNull
    private ShoppingCartDto shoppingCart;

    @NotNull
    private AddressDto deliveryAddress;
}
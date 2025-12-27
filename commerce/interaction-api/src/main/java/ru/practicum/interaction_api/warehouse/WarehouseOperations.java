package ru.practicum.interaction_api.warehouse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.practicum.interaction_api.shopping_cart.dto.ShoppingCartDto;
import ru.practicum.interaction_api.warehouse.dto.AddressDto;
import ru.practicum.interaction_api.warehouse.dto.BookedProductsDto;

public interface WarehouseOperations {

    @GetMapping("/api/v1/warehouse/address")
    AddressDto getAddress();

    @GetMapping("/api/v1/warehouse/check")
    BookedProductsDto checkQuantityForCart(@RequestBody ShoppingCartDto shoppingCartDto);
}
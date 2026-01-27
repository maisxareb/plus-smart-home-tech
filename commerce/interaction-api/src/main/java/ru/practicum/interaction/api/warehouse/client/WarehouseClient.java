package ru.practicum.interaction.api.warehouse.client;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.practicum.interaction.api.shopping.cart.dto.ShoppingCartDto;
import ru.practicum.interaction.api.warehouse.dto.AddressDto;
import ru.practicum.interaction.api.warehouse.dto.AssemblyProductsForOrderRequest;
import ru.practicum.interaction.api.warehouse.dto.BookedProductsDto;
import ru.practicum.interaction.api.warehouse.dto.ShippedToDeliveryRequest;

import java.util.Map;
import java.util.UUID;

@FeignClient(name = "warehouse")
public interface WarehouseClient {

    @PostMapping("/api/v1/warehouse/assembly")
    BookedProductsDto assemblyProductForOrderFromShoppingCart(@RequestBody @Valid ShoppingCartDto shoppingCartDto);

    @GetMapping("/api/v1/warehouse/address")
    AddressDto getWarehouseAddress();

    @PostMapping("/api/v1/warehouse/returnProducts")
    void returnProducts(@RequestBody Map<UUID, Integer> products);

    @PostMapping("/api/v1/warehouse/assembly")
    BookedProductsDto assemblyProducts(@RequestBody @Valid AssemblyProductsForOrderRequest request);

    @PostMapping("/api/v1/warehouse/shipped")
    void shippedOrder(@RequestBody @Valid ShippedToDeliveryRequest request);

    @PostMapping("/api/v1/warehouse/check")
    BookedProductsDto checkQuantityForCart(@RequestBody ShoppingCartDto shoppingCart);
}
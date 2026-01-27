package ru.practicum.warehouse.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.interaction.api.shopping.cart.dto.ShoppingCartDto;
import ru.practicum.interaction.api.warehouse.dto.AddressDto;
import ru.practicum.interaction.api.warehouse.dto.BookedProductsDto;
import ru.practicum.interaction.api.warehouse.dto.ProductInWarehouseDto;
import ru.practicum.warehouse.model.AddProductToWarehouseRequest;
import ru.practicum.interaction.api.warehouse.dto.AssemblyProductsForOrderRequest;
import ru.practicum.warehouse.model.NewProductInWarehouseRequest;
import ru.practicum.interaction.api.warehouse.dto.ShippedToDeliveryRequest;
import ru.practicum.warehouse.service.WarehouseService;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/warehouse")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService service;

    @GetMapping("/address")
    public AddressDto getAddress() {
        return service.getAddress();
    }

    @PutMapping
    public ProductInWarehouseDto addNewProduct(@RequestBody NewProductInWarehouseRequest newProductInWarehouseRequest) {
        return service.addNewProduct(newProductInWarehouseRequest);
    }

    @PostMapping("/check")
    public BookedProductsDto checkQuantityForCart(@RequestBody ShoppingCartDto shoppingCart) {
        return service.checkQuantityForCart(shoppingCart);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/add")
    public void acceptProduct(@RequestBody @Valid AddProductToWarehouseRequest request) {
        service.acceptProduct(request);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/shipped")
    public void shippedOrder(@RequestBody @Valid ShippedToDeliveryRequest request) {
        service.shippedProducts(request);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/returnProducts")
    public void returnProducts(@RequestBody @Valid Map<UUID, Integer> products) {
        service.returnProducts(products);
    }

    @PostMapping("/assembly")
    public BookedProductsDto assemblyProducts(@RequestBody AssemblyProductsForOrderRequest request) {
        return service.assemblyProducts(request);
    }
}
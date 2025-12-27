package ru.practicum.warehouse.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.interaction_api.shopping_cart.dto.ShoppingCartDto;
import ru.practicum.interaction_api.warehouse.WarehouseOperations;
import ru.practicum.interaction_api.warehouse.dto.AddressDto;
import ru.practicum.interaction_api.warehouse.dto.BookedProductsDto;
import ru.practicum.interaction_api.warehouse.dto.ProductInWarehouseDto;
import ru.practicum.warehouse.model.AddProductToWarehouseRequest;
import ru.practicum.warehouse.model.NewProductInWarehouseRequest;
import ru.practicum.warehouse.service.WarehouseService;

@RestController
@RequestMapping("/api/v1/warehouse")
@RequiredArgsConstructor
public class WarehouseController implements WarehouseOperations {

    private final WarehouseService service;

    @Override
    @GetMapping("/address")
    public AddressDto getAddress() {
        return service.getAddress();
    }

    @Override
    @PostMapping("/check")
    public BookedProductsDto checkQuantityForCart(@RequestBody ShoppingCartDto shoppingCart) {
        return service.checkQuantityForCart(shoppingCart);
    }

    // Дополнительные методы, не входящие в клиентский контракт
    @PutMapping
    public ProductInWarehouseDto addNewProduct(@RequestBody NewProductInWarehouseRequest newProductInWarehouseRequest) {
        return service.addNewProduct(newProductInWarehouseRequest);
    }

    @PostMapping("/add")
    public void acceptProduct(@RequestBody @Valid AddProductToWarehouseRequest request) {
        service.acceptProduct(request);
    }
}
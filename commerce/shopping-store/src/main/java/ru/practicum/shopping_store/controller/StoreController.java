package ru.practicum.shopping_store.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;
import ru.practicum.interaction_api.shopping_store.dto.ProductDto;
import ru.practicum.shopping_store.model.*;
import ru.practicum.shopping_store.service.ProductService;


@RestController
@RequestMapping("/api/v1/shopping-store")
@RequiredArgsConstructor
public class StoreController {

    private final ProductService service;

    @GetMapping
    public Page<ProductDto> getProducts(
            @RequestParam ProductCategory category,
            @PageableDefault(sort = {"productName"}) Pageable pageable
    ) {
        return service.getProducts(category, pageable);
    }

    @GetMapping("/{productId}")
    public ProductDto getProductById(@PathVariable String productId) {
        return service.getProductById(productId);
    }

    @PutMapping
    public ProductDto createProduct(@RequestBody @Valid ProductDto productDto) {
        return service.createProduct(productDto);
    }

    @PostMapping
    public ProductDto updateProduct(@RequestBody @Valid ProductDto productDto) {
        return service.updateProduct(productDto);
    }

    @PostMapping("/removeProductFromStore")
    public Boolean removeProduct(@RequestBody String productId) {
        return service.removeProduct(productId.replaceAll("^\"|\"$", ""));
    }

    @PostMapping("/quantityState")
    public Boolean setQuantity(@RequestParam String productId, @RequestParam QuantityState quantityState) {
        return service.setQuantity(UpdateStockLevelRequest.builder()
                .productId(productId)
                .quantityState(quantityState)
                .build());
    }

}

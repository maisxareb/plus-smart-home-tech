package ru.practicum.interaction.api.shopping.store.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.practicum.interaction.api.shopping.store.dto.ProductDto;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "shopping-store")
public interface ShoppingStoreClient {

    @GetMapping("/api/v1/shopping-store/{productId}")
    ProductDto getProductById(@PathVariable("productId") UUID productId);

    @PostMapping("/products/batch")
    List<ProductDto> getProductsByIds(@RequestBody List<UUID> productIds);
}
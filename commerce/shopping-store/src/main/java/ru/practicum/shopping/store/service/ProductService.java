package ru.practicum.shopping.store.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.practicum.interaction.api.shopping.store.dto.ProductDto;
import ru.practicum.shopping.store.model.ProductCategory;
import ru.practicum.shopping.store.model.UpdateStockLevelRequest;

import java.util.UUID;

public interface ProductService {

    Page<ProductDto> getProducts(ProductCategory category, Pageable pageable);

    ProductDto getProductById(UUID productId);

    ProductDto createProduct(ProductDto productDto);

    ProductDto updateProduct(ProductDto productDto);

    Boolean removeProduct(UUID productId);

    Boolean setQuantity(UpdateStockLevelRequest request);
}
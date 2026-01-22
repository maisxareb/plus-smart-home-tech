package ru.practicum.shopping.store.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.interaction.api.shopping.store.dto.ProductDto;
import ru.practicum.shopping.store.model.ProductCategory;
import ru.practicum.shopping.store.model.UpdateStockLevelRequest;

@Service
public interface ProductService {

    Page<ProductDto> getProducts(ProductCategory category, Pageable pageable);

    ProductDto getProductById(String productId);

    ProductDto createProduct(ProductDto productDto);

    ProductDto updateProduct(ProductDto productDto);

    Boolean removeProduct(String productId);

    Boolean setQuantity(UpdateStockLevelRequest request);
}
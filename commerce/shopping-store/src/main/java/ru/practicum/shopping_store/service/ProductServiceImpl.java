package ru.practicum.shopping_store.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.interaction_api.shopping_store.dto.ProductDto;
import ru.practicum.shopping_store.exception.ProductNotFoundException;
import ru.practicum.shopping_store.mapper.ProductMapper;
import ru.practicum.shopping_store.model.*;
import ru.practicum.shopping_store.repository.ProductRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repository;
    private final ProductMapper mapper;

    @Override
    public Page<ProductDto> getProducts(ProductCategory category, Pageable pageable) {
        Page<Product> products = repository.findAllByProductCategory(category, pageable);
        return products.map(mapper::toDto);
    }

    @Override
    public ProductDto getProductById(String productId) {
        Product product = productExists(productId);
        return mapper.toDto(product);
    }

    @Override
    public ProductDto createProduct(ProductDto productDto) {
        Product newProduct = mapper.toEntity(productDto);
        Product savedProduct = repository.save(newProduct);
        return mapper.toDto(savedProduct);
    }

    @Override
    public ProductDto updateProduct(ProductDto productDto) {
        Product oldProduct = productExists(productDto.getProductId());

        mapper.updateEntityFromDto(productDto, oldProduct);
        Product updatedProduct = repository.save(oldProduct);
        return mapper.toDto(updatedProduct);
    }

    @Override
    public Boolean removeProduct(String productId) {
        Product product = productExists(productId);

        product.setProductState(ProductState.DEACTIVATE);
        repository.save(product);
        return true;
    }

    @Override
    public Boolean setQuantity(UpdateStockLevelRequest request) {
        Product product = productExists(request.getProductId());

        product.setQuantityState(request.getQuantityState());
        repository.save(product);
        return true;
    }

    private Product productExists(String productId) {
        try {
            return repository.findById(productId)
                    .orElseThrow(() -> new ProductNotFoundException("Товар с id " + productId + " не найден!"));
        } catch (ProductNotFoundException e) {
            log.error("Ошибка поиска товара с id {}: ", productId, e);
            throw e;
        }
    }
}
package ru.practicum.shopping.store.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.interaction.api.shopping.store.dto.ProductDto;
import ru.practicum.interaction.api.shopping.store.exception.ProductNotFoundException;
import ru.practicum.shopping.store.mapper.ProductMapper;
import ru.practicum.shopping.store.model.Product;
import ru.practicum.shopping.store.model.ProductCategory;
import ru.practicum.shopping.store.model.ProductState;
import ru.practicum.shopping.store.model.UpdateStockLevelRequest;
import ru.practicum.shopping.store.repository.ProductRepository;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repository;
    private final ProductMapper productMapper;

    @Override
    public Page<ProductDto> getProducts(ProductCategory category, Pageable pageable) {
        Page<Product> products = repository.findAllByProductCategory(category, pageable);
        return products.map(productMapper::toDto);
    }

    @Override
    public ProductDto getProductById(UUID productId) {
        return productMapper.toDto(productExists(productId));
    }

    @Override
    public ProductDto createProduct(ProductDto productDto) {
        Product newProduct = productMapper.toEntity(productDto);
        return productMapper.toDto(repository.save(newProduct));
    }

    @Override
    public ProductDto updateProduct(ProductDto productDto) {
        Product oldProduct = productExists(productDto.getProductId());
        productMapper.updateEntityFromDto(productDto, oldProduct);
        return productMapper.toDto(repository.save(oldProduct));
    }

    @Override
    public Boolean removeProduct(UUID productId) {
        Product product = productExists(productId);
        product.setProductState(ProductState.DEACTIVATE);
        repository.save(product);
        return true;
    }

    @Override
    public Boolean setQuantity(UpdateStockLevelRequest request) {
        Product product = productExists(request.getProductId());
        return true;
    }

    private Product productExists(UUID productId) {
        try {
            return repository.findById(productId)
                    .orElseThrow(() -> new ProductNotFoundException("Товар с id " + productId + " не найден!"));
        } catch (ProductNotFoundException e) {
            log.error("Ошибка поиска товара с id {}: ", productId, e);
            throw e;
        }
    }
}
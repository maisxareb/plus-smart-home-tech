package ru.practicum.warehouse.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.interaction_api.shopping_cart.dto.ShoppingCartDto;
import ru.practicum.interaction_api.warehouse.dto.AddressDto;
import ru.practicum.interaction_api.warehouse.dto.BookedProductsDto;
import ru.practicum.interaction_api.warehouse.dto.ProductInWarehouseDto;
import ru.practicum.interaction_api.warehouse.dto.DimensionDto;
import ru.practicum.warehouse.Warehouse;
import ru.practicum.warehouse.exception.InsufficientStockException;
import ru.practicum.warehouse.exception.ProductNotFoundException;
import ru.practicum.warehouse.exception.ProductAlreadyExistsException;
import ru.practicum.warehouse.model.AddProductToWarehouseRequest;
import ru.practicum.warehouse.model.NewProductInWarehouseRequest;
import ru.practicum.warehouse.model.ProductInWarehouse;
import ru.practicum.warehouse.mapper.ProductInWarehouseMapper;
import ru.practicum.warehouse.repository.WarehouseRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository repository;
    private final ProductInWarehouseMapper mapper;

    @Override
    public ProductInWarehouseDto addNewProduct(NewProductInWarehouseRequest newProduct) {
        if (isProductInWarehouse(newProduct.getProductId())) {
            throw new ProductAlreadyExistsException("Продукт с id " + newProduct.getProductId() + " уже добавлен на склад!");
        }

        ProductInWarehouse entity = mapper.toEntity(newProduct);
        ProductInWarehouse savedEntity = repository.save(entity);

        return mapper.toDto(savedEntity);
    }

    @Override
    public BookedProductsDto checkQuantityForCart(ShoppingCartDto shoppingCart) {
        BookedProductsDto bookedProductsDto = BookedProductsDto.builder().build();

        shoppingCart.getProducts().forEach((productId, quantity) -> {
            ProductInWarehouse productInWarehouse = productInWarehouseExists(productId);

            if (quantity > productInWarehouse.getQuantity()) {
                throw new InsufficientStockException("Товара с id " + productId + " в корзине больше, чем доступно на складе!");
            }

            bookedProductsDto.setDeliveryWeight(bookedProductsDto.getDeliveryWeight() + productInWarehouse.getWeight());
            bookedProductsDto.setDeliveryVolume(bookedProductsDto.getDeliveryVolume() + calculateVolume(productInWarehouse));
        });

        return bookedProductsDto;
    }

    @Override
    public void acceptProduct(AddProductToWarehouseRequest request) {
        ProductInWarehouse productInWarehouse = productInWarehouseExists(request.getProductId());
        productInWarehouse.setQuantity(productInWarehouse.getQuantity() + request.getQuantity());

        repository.save(productInWarehouse);
    }

    @Override
    public AddressDto getAddress() {
        return Warehouse.getRandomAddress();
    }

    private boolean isProductInWarehouse(String productId) {
        return repository.existsById(productId);
    }

    private Double calculateVolume(ProductInWarehouse product) {
        DimensionDto dimension = product.getDimension();
        return dimension.getHeight() * dimension.getDepth() * dimension.getWidth();
    }

    private ProductInWarehouse productInWarehouseExists(String productId) {
        return repository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Продукт с id " + productId + " не найден на складе!"));
    }
}
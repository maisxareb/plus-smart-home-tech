package ru.practicum.warehouse.service;

import org.springframework.stereotype.Service;
import ru.practicum.interaction.api.shopping.cart.dto.ShoppingCartDto;
import ru.practicum.interaction.api.warehouse.dto.AddressDto;
import ru.practicum.interaction.api.warehouse.dto.BookedProductsDto;
import ru.practicum.interaction.api.warehouse.dto.ProductInWarehouseDto;
import ru.practicum.warehouse.model.AddProductToWarehouseRequest;
import ru.practicum.warehouse.model.NewProductInWarehouseRequest;

@Service
public interface WarehouseService {

    ProductInWarehouseDto addNewProduct(NewProductInWarehouseRequest newProductInWarehouseRequest);

    BookedProductsDto checkQuantityForCart(ShoppingCartDto shoppingCart);

    void acceptProduct(AddProductToWarehouseRequest request);

    AddressDto getAddress();
}
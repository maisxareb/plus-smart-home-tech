package ru.practicum.warehouse.service;

import ru.practicum.interaction.api.shopping.cart.dto.ShoppingCartDto;
import ru.practicum.interaction.api.warehouse.dto.AddressDto;
import ru.practicum.interaction.api.warehouse.dto.BookedProductsDto;
import ru.practicum.interaction.api.warehouse.dto.ProductInWarehouseDto;
import ru.practicum.warehouse.model.AddProductToWarehouseRequest;
import ru.practicum.interaction.api.warehouse.dto.AssemblyProductsForOrderRequest;
import ru.practicum.warehouse.model.NewProductInWarehouseRequest;
import ru.practicum.interaction.api.warehouse.dto.ShippedToDeliveryRequest;

import java.util.Map;
import java.util.UUID;

public interface WarehouseService {

    ProductInWarehouseDto addNewProduct(NewProductInWarehouseRequest newProductInWarehouseRequest);

    BookedProductsDto checkQuantityForCart(ShoppingCartDto shoppingCart);

    void acceptProduct(AddProductToWarehouseRequest request);

    AddressDto getAddress();

    void shippedProducts(ShippedToDeliveryRequest request);

    void returnProducts(Map<UUID, Integer> products);

    BookedProductsDto assemblyProducts(AssemblyProductsForOrderRequest request);
}
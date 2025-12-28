package ru.practicum.shopping.cart.service;

import org.springframework.stereotype.Service;
import ru.practicum.interaction.api.shopping.cart.dto.ShoppingCartDto;
import ru.practicum.shopping.cart.model.UpdateProductQuantityRequest;

import java.util.List;
import java.util.Map;

@Service
public interface CartService {

    ShoppingCartDto getCart(String username);

    ShoppingCartDto addProductToCart(String username, Map<String, Integer> products);

    void deactivateCart(String username);

    ShoppingCartDto removeProductFromCart(String username, List<String> products);

    ShoppingCartDto changeProductQuantity(String username, UpdateProductQuantityRequest request);
}
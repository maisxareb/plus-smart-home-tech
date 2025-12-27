package ru.practicum.shopping_cart.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.interaction_api.shopping_cart.dto.ShoppingCartDto;
import ru.practicum.interaction_api.warehouse.client.WarehouseClient;
import ru.practicum.shopping_cart.exception.CartNotFoundException;
import ru.practicum.shopping_cart.exception.CartWasDeactivatedException;
import ru.practicum.shopping_cart.exception.UnauthorizedUserException;
import ru.practicum.shopping_cart.mapper.CartMapper;
import ru.practicum.shopping_cart.model.*;
import ru.practicum.shopping_cart.repository.CartRepository;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository repository;
    private final WarehouseClient client;
    private final CartMapper mapper;

    @Override
    public ShoppingCartDto getCart(String username) {
        if (username == null) {
            throw new UnauthorizedUserException("Имя пользователя не может быть пустым!");
        }

        Cart cart = cartExistsByUsername(username);
        return mapper.toDto(cart);
    }

    @Override
    public ShoppingCartDto addProductToCart(String username, Map<String, Integer> products) {
        if (username == null) {
            throw new UnauthorizedUserException("Имя пользователя не может быть пустым!");
        }

        try {
            Cart shoppingCart = cartExistsByUsername(username);

            ShoppingCartDto cartDto = mapper.toDto(shoppingCart);
            client.checkQuantityForCart(cartDto);

            Cart updated = addProductsToCart(shoppingCart, products);

            return mapper.toDto(repository.save(updated));
        } catch (CartNotFoundException e) {
            Cart newShoppingCart = Cart.builder()
                    .items(new ArrayList<>())
                    .owner(username)
                    .build();

            Cart updated = addProductsToCart(newShoppingCart, products);

            return mapper.toDto(repository.save(updated));
        }
    }

    @Override
    public void deactivateCart(String username) {
        if (username == null) {
            throw new UnauthorizedUserException("Имя пользователя не может быть пустым!");
        }

        Cart shoppingCart = cartExistsByUsername(username);
        shoppingCart.setState(CartStatus.DEACTIVATED);

        repository.save(shoppingCart);
    }

    @Override
    public ShoppingCartDto removeProductFromCart(String username, List<String> products) {
        if (username == null) {
            throw new UnauthorizedUserException("Имя пользователя не может быть пустым!");
        }

        Cart shoppingCart = cartExistsByUsername(username);
        shoppingCart.getItems().removeIf(item -> products.contains(item.getProductId()));

        return mapper.toDto(repository.save(shoppingCart));
    }

    @Override
    public ShoppingCartDto changeProductQuantity(String username, UpdateProductQuantityRequest request) {
        if (username == null) {
            throw new UnauthorizedUserException("Имя пользователя не может быть пустым!");
        }

        Cart shoppingCart = cartExistsByUsername(username);

        for (CartItem item : shoppingCart.getItems()) {
            if (item.getProductId().equals(request.getProductId())) {
                item.setQuantity(request.getNewQuantity());
                break;
            }
        }

        ShoppingCartDto cartDto = mapper.toDto(shoppingCart);
        client.checkQuantityForCart(cartDto);

        return mapper.toDto(repository.save(shoppingCart));
    }

    private Cart cartExistsByUsername(String username) {
        Cart shoppingCart = repository.findByOwner(username)
                .orElseThrow(() -> new CartNotFoundException("Корзина для пользователя " + username + " не найдена!"));

        if (shoppingCart.getState().equals(CartStatus.DEACTIVATED)) {
            throw new CartWasDeactivatedException("Корзина была диактивирована!");
        }

        return shoppingCart;
    }

    private Cart addProductsToCart(Cart shoppingCart, Map<String, Integer> products) {
        Map<String, Integer> validProducts = new HashMap<>(products);
        Map<String, CartItem> itemMap = shoppingCart.getItems().stream()
                .collect(Collectors.toMap(CartItem::getProductId, Function.identity()));

        validProducts.forEach((productId, quantity) -> {
            if (itemMap.containsKey(productId)) {
                CartItem existingItem = itemMap.get(productId);
                existingItem.setQuantity(existingItem.getQuantity() + quantity);
            } else {
                CartItem newItem = CartItem.builder()
                        .shoppingCart(shoppingCart)
                        .productId(productId)
                        .quantity(quantity)
                        .build();
                shoppingCart.getItems().add(newItem);
            }
        });

        return shoppingCart;
    }
}
package ru.practicum.shopping.cart.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.interaction.api.order.exception.NotAuthorizedUserException;
import ru.practicum.interaction.api.shopping.cart.dto.ShoppingCartDto;
import ru.practicum.interaction.api.warehouse.client.WarehouseClient;
import ru.practicum.shopping.cart.repository.CartRepository;
import ru.practicum.shopping.cart.exception.CartNotFoundException;
import ru.practicum.shopping.cart.exception.CartWasDeactivated;
import ru.practicum.shopping.cart.model.*;
import ru.practicum.shopping.cart.mapper.CartMapper;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository repository;
    private final WarehouseClient client;
    private final CartMapper cartMapper;

    @Override
    public ShoppingCartDto getCart(String username) {
        if (username == null) {
            throw new NotAuthorizedUserException("Имя пользователя не может быть пустым!");
        }

        Cart cart = cartExistsByUsername(username);
        return cartMapper.toDto(cart);
    }

    @Override
    public ShoppingCartDto addProductToCart(String username, Map<UUID, Integer> products) {
        if (username == null) {
            throw new NotAuthorizedUserException("Имя пользователя не может быть пустым!");
        }

        try {
            Cart shoppingCart = cartExistsByUsername(username);
            client.checkQuantityForCart(cartMapper.toDto(shoppingCart));
            Cart updated = addProductsToCart(shoppingCart, products);

            Cart savedCart = repository.save(updated);
            return cartMapper.toDto(savedCart);
        }
        catch (CartNotFoundException e) {
            Cart newShoppingCart = Cart.builder()
                    .items(new ArrayList<>())
                    .owner(username)
                    .build();

            Cart updated = addProductsToCart(newShoppingCart, products);
            Cart savedCart = repository.save(updated);
            return cartMapper.toDto(savedCart);
        }
    }

    @Override
    public void deactivateCart(String username) {
        if (username == null) {
            throw new NotAuthorizedUserException("Имя пользователя не может быть пустым!");
        }

        Cart shoppingCart = cartExistsByUsername(username);
        shoppingCart.setState(CartStatus.DEACTIVATED);

        repository.save(shoppingCart);
    }

    @Override
    public ShoppingCartDto removeProductFromCart(String username, List<UUID> products) {
        if (username == null) {
            throw new NotAuthorizedUserException("Имя пользователя не может быть пустым!");
        }

        Cart shoppingCart = cartExistsByUsername(username);
        shoppingCart.getItems().removeIf(item -> products.contains(item.getProductId()));

        Cart savedCart = repository.save(shoppingCart);
        return cartMapper.toDto(savedCart);
    }

    @Override
    public ShoppingCartDto changeProductQuantity(String username, UpdateProductQuantityRequest request) {
        if (username == null) {
            throw new NotAuthorizedUserException("Имя пользователя не может быть пустым!");
        }

        Cart shoppingCart = cartExistsByUsername(username);

        for (CartItem item : shoppingCart.getItems()) {
            if (item.getProductId().equals(request.getProductId())) {
                item.setQuantity(request.getNewQuantity());
                break;
            }
        }
        client.assemblyProductForOrderFromShoppingCart(cartMapper.toDto(shoppingCart));

        Cart savedCart = repository.save(shoppingCart);
        return cartMapper.toDto(savedCart);
    }

    private Cart cartExistsByUsername(String username) {
        Cart shoppingCart = repository.findByOwner(username)
                .orElseThrow(() -> new CartNotFoundException("Корзина для пользователя " + username + " не найдена!"));

        if (shoppingCart.getState().equals(CartStatus.DEACTIVATED)) {
            throw new CartWasDeactivated("Корзина была деактивирована!");
        }

        return shoppingCart;
    }

    private Cart addProductsToCart(Cart shoppingCart, Map<UUID, Integer> products) {

        Map<UUID, Integer> validProducts = new HashMap<>(products);
        Map<UUID, CartItem> itemMap = shoppingCart.getItems().stream()
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
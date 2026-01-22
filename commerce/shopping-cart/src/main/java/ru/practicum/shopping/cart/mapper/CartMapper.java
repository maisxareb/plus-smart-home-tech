package ru.practicum.shopping.cart.mapper;

import org.mapstruct.*;
import ru.practicum.interaction.api.shopping.cart.dto.ShoppingCartDto;
import ru.practicum.shopping.cart.model.Cart;
import ru.practicum.shopping.cart.model.CartItem;

import java.util.*;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public abstract class CartMapper {

    public ShoppingCartDto toDto(Cart shoppingCart) {
        if (shoppingCart == null) {
            return null;
        }

        return ShoppingCartDto.builder()
                .shoppingCartId(shoppingCart.getShoppingCartId())
                .products(itemsToMap(shoppingCart.getItems()))
                .build();
    }

    public abstract Cart toEntity(ShoppingCartDto shoppingCartDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "shoppingCartId", ignore = true)
    public abstract void updateEntityFromDto(ShoppingCartDto dto, @MappingTarget Cart entity);

    protected Map<String, Integer> itemsToMap(List<CartItem> items) {
        if (items == null || items.isEmpty()) {
            return Collections.emptyMap();
        }
        return items.stream()
                .collect(Collectors.toMap(
                        CartItem::getProductId,
                        CartItem::getQuantity
                ));
    }

    protected List<CartItem> mapToItems(Map<String, Integer> products) {
        if (products == null || products.isEmpty()) {
            return Collections.emptyList();
        }
        return products.entrySet().stream()
                .map(entry -> {
                    CartItem item = new CartItem();
                    item.setProductId(entry.getKey());
                    item.setQuantity(entry.getValue());
                    return item;
                })
                .collect(Collectors.toList());
    }
}
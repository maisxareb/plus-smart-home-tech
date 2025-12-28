package ru.practicum.shopping.cart.mapper;

import org.mapstruct.*;
import ru.practicum.interaction.api.shopping.cart.dto.ShoppingCartDto;
import ru.practicum.shopping.cart.model.Cart;
import ru.practicum.shopping.cart.model.CartItem;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface CartMapper {

    @Mapping(target = "products", source = "items", qualifiedByName = "itemsToMap")
    ShoppingCartDto toDto(Cart shoppingCart);

    @Mapping(target = "items", source = "products", qualifiedByName = "mapToItems")
    Cart toEntity(ShoppingCartDto shoppingCartDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "shoppingCartId", ignore = true)
    @Mapping(target = "items", source = "products", qualifiedByName = "mapToItems")
    void updateEntityFromDto(ShoppingCartDto dto, @MappingTarget Cart entity);

    @Named("itemsToMap")
    default Map<String, Integer> itemsToMap(List<CartItem> items) {
        if (items == null) {
            return null;
        }
        return items.stream()
                .collect(Collectors.toMap(
                        CartItem::getProductId,
                        CartItem::getQuantity
                ));
    }

    @Named("mapToItems")
    default List<CartItem> mapToItems(Map<String, Integer> products) {
        if (products == null) {
            return null;
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
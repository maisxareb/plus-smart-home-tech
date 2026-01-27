package ru.practicum.order.mapper;

import org.mapstruct.*;
import ru.practicum.interaction.api.order.dto.OrderDto;
import ru.practicum.order.model.Order;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface OrderMapper {

    OrderDto toDto(Order order);

    @Mapping(target = "username", source = "username")
    @Mapping(target = "orderId", source = "dto.orderId")
    @Mapping(target = "shoppingCartId", source = "dto.shoppingCartId")
    @Mapping(target = "products", source = "dto.products")
    @Mapping(target = "paymentId", source = "dto.paymentId")
    @Mapping(target = "deliveryId", source = "dto.deliveryId")
    @Mapping(target = "state", source = "dto.state")
    @Mapping(target = "deliveryWeight", source = "dto.deliveryWeight")
    @Mapping(target = "deliveryVolume", source = "dto.deliveryVolume")
    @Mapping(target = "fragile", source = "dto.fragile")
    @Mapping(target = "totalPrice", source = "dto.totalPrice")
    @Mapping(target = "deliveryPrice", source = "dto.deliveryPrice")
    @Mapping(target = "productPrice", source = "dto.productPrice")
    Order fromDto(OrderDto dto, String username);
}
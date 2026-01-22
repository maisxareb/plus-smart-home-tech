package ru.practicum.warehouse.mapper;

import org.mapstruct.*;
import ru.practicum.interaction.api.warehouse.dto.BookedProductsDto;
import ru.practicum.warehouse.model.OrderBooking;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_DEFAULT)
public interface OrderBookingMapper {

    @Mapping(target = "deliveryWeight", source = "deliveryWeight")
    @Mapping(target = "deliveryVolume", source = "deliveryVolume")
    @Mapping(target = "fragile", source = "fragile")
    BookedProductsDto toDto(OrderBooking booking);

    @Mapping(target = "deliveryWeight", source = "deliveryWeight",
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_DEFAULT)
    @Mapping(target = "deliveryVolume", source = "deliveryVolume",
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_DEFAULT)
    @Mapping(target = "fragile", source = "fragile",
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_DEFAULT)
    OrderBooking toEntity(BookedProductsDto dto);
}
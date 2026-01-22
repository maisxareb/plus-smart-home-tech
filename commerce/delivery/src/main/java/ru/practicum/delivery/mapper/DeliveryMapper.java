package ru.practicum.delivery.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.practicum.delivery.model.Delivery;
import ru.practicum.delivery.model.DeliveryAddress;
import ru.practicum.interaction.api.delivery.dto.DeliveryDto;
import ru.practicum.interaction.api.warehouse.dto.AddressDto;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface DeliveryMapper {

    DeliveryDto toDto(Delivery entity);

    Delivery toEntity(DeliveryDto dto);

    AddressDto addressToDto(DeliveryAddress address);

    DeliveryAddress dtoToAddress(AddressDto dto);
}
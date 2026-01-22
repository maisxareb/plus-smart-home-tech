package ru.practicum.delivery.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.practicum.delivery.model.Delivery;
import ru.practicum.delivery.model.DeliveryAddress;
import ru.practicum.interaction.api.delivery.dto.DeliveryDto;
import ru.practicum.interaction.api.delivery.dto.DeliveryState;
import ru.practicum.interaction.api.warehouse.dto.AddressDto;

import java.util.UUID;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface DeliveryMapper {

    @Mapping(source = "fromAddress", target = "fromAddress")
    @Mapping(source = "toAddress", target = "toAddress")
    @Mapping(source = "deliveryState", target = "deliveryState")
    DeliveryDto toDto(Delivery entity);

    @Mapping(source = "fromAddress", target = "fromAddress")
    @Mapping(source = "toAddress", target = "toAddress")
    @Mapping(source = "deliveryState", target = "deliveryState")
    @Mapping(target = "deliveryId", expression = "java(generateDeliveryIdIfNull(dto.getDeliveryId()))")
    Delivery toEntity(DeliveryDto dto);

    AddressDto addressToDto(DeliveryAddress address);

    DeliveryAddress dtoToAddress(AddressDto dto);

    default DeliveryState mapDeliveryState(DeliveryState state) {
        return state != null ? state : DeliveryState.CREATED;
    }

    default UUID generateDeliveryIdIfNull(UUID deliveryId) {
        return deliveryId != null ? deliveryId : UUID.randomUUID();
    }
}
package ru.practicum.warehouse.mapper;

import org.mapstruct.*;
import ru.practicum.interaction.api.warehouse.dto.ProductInWarehouseDto;
import ru.practicum.warehouse.model.NewProductInWarehouseRequest;
import ru.practicum.warehouse.model.ProductInWarehouse;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductInWarehouseMapper {

    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "fragile", source = "fragile")
    @Mapping(target = "dimension", source = "dimension")
    @Mapping(target = "weight", source = "weight")
    ProductInWarehouseDto toDto(ProductInWarehouse entity);

    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "fragile", source = "fragile")
    @Mapping(target = "dimension", source = "dimension")
    @Mapping(target = "weight", source = "weight")
    ProductInWarehouse toEntity(NewProductInWarehouseRequest newProduct);
}
package ru.practicum.shopping.store.mapper;

import org.mapstruct.*;
import ru.practicum.interaction.api.shopping.store.dto.ProductDto;
import ru.practicum.shopping.store.model.Product;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper {

    ProductDto toDto(Product product);

    Product toEntity(ProductDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(ProductDto dto, @MappingTarget Product entity);
}
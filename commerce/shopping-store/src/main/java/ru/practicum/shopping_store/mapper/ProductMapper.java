package ru.practicum.shopping_store.mapper;

import org.mapstruct.*;
import ru.practicum.interaction_api.shopping_store.dto.ProductDto;
import ru.practicum.shopping_store.model.Product;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface ProductMapper {

    @Mapping(target = "quantityState", expression = "java(ru.practicum.interaction_api.shopping_store.dto.QuantityState.valueOf(product.getQuantityState().name()))")
    @Mapping(target = "productState", expression = "java(ru.practicum.interaction_api.shopping_store.dto.ProductState.valueOf(product.getProductState().name()))")
    @Mapping(target = "productCategory", expression = "java(ru.practicum.interaction_api.shopping_store.dto.ProductCategory.valueOf(product.getProductCategory().name()))")
    ProductDto toDto(Product product);

    @Mapping(target = "quantityState", expression = "java(ru.practicum.shopping_store.model.QuantityState.valueOf(dto.getQuantityState().name()))")
    @Mapping(target = "productState", expression = "java(ru.practicum.shopping_store.model.ProductState.valueOf(dto.getProductState().name()))")
    @Mapping(target = "productCategory", expression = "java(ru.practicum.shopping_store.model.ProductCategory.valueOf(dto.getProductCategory().name()))")
    Product toEntity(ProductDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "productId", ignore = true)
    @Mapping(target = "quantityState", expression = "java(newData.getQuantityState() != null ? " +
            "ru.practicum.shopping_store.model.QuantityState.valueOf(newData.getQuantityState().name()) : null)")
    @Mapping(target = "productState", expression = "java(newData.getProductState() != null ? " +
            "ru.practicum.shopping_store.model.ProductState.valueOf(newData.getProductState().name()) : null)")
    @Mapping(target = "productCategory", expression = "java(newData.getProductCategory() != null ? " +
            "ru.practicum.shopping_store.model.ProductCategory.valueOf(newData.getProductCategory().name()) : null)")
    void updateEntityFromDto(ProductDto newData, @MappingTarget Product entity);

    default Product updateFields(Product oldProduct, ProductDto newData) {
        if (oldProduct == null || newData == null) {
            return oldProduct;
        }

        Product updatedProduct = new Product();
        updatedProduct.setProductId(oldProduct.getProductId());
        updatedProduct.setProductName(oldProduct.getProductName());
        updatedProduct.setDescription(oldProduct.getDescription());
        updatedProduct.setImageSrc(oldProduct.getImageSrc());
        updatedProduct.setPrice(oldProduct.getPrice());
        updatedProduct.setQuantityState(oldProduct.getQuantityState());
        updatedProduct.setProductState(oldProduct.getProductState());
        updatedProduct.setProductCategory(oldProduct.getProductCategory());

        updateEntityFromDto(newData, updatedProduct);

        return updatedProduct;
    }
}
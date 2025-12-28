package ru.practicum.shopping.store.mapper;

import org.mapstruct.*;
import ru.practicum.interaction.api.shopping.store.dto.ProductDto;
import ru.practicum.shopping.store.model.Product;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface ProductMapper {

    @Mapping(target = "quantityState", expression = "java(dto.shopping_store.ru.practicum.interaction.api.QuantityState.valueOf(product.getQuantityState().name()))")
    @Mapping(target = "productState", expression = "java(dto.shopping_store.ru.practicum.interaction.api.ProductState.valueOf(product.getProductState().name()))")
    @Mapping(target = "productCategory", expression = "java(dto.shopping_store.ru.practicum.interaction.api.ProductCategory.valueOf(product.getProductCategory().name()))")
    ProductDto toDto(Product product);

    @Mapping(target = "quantityState", expression = "java(model.ru.practicum.shopping.store.QuantityState.valueOf(dto.getQuantityState().name()))")
    @Mapping(target = "productState", expression = "java(model.ru.practicum.shopping.store.ProductState.valueOf(dto.getProductState().name()))")
    @Mapping(target = "productCategory", expression = "java(model.ru.practicum.shopping.store.ProductCategory.valueOf(dto.getProductCategory().name()))")
    Product toEntity(ProductDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "productId", ignore = true)
    @Mapping(target = "quantityState", expression = "java(newData.getQuantityState() != null ? " +
            "model.ru.practicum.shopping.store.QuantityState.valueOf(newData.getQuantityState().name()) : null)")
    @Mapping(target = "productState", expression = "java(newData.getProductState() != null ? " +
            "model.ru.practicum.shopping.store.ProductState.valueOf(newData.getProductState().name()) : null)")
    @Mapping(target = "productCategory", expression = "java(newData.getProductCategory() != null ? " +
            "model.ru.practicum.shopping.store.ProductCategory.valueOf(newData.getProductCategory().name()) : null)")
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
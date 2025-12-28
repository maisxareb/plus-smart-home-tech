package ru.practicum.shopping.store.mapper;

import org.mapstruct.*;
import ru.practicum.interaction.api.shopping.store.dto.ProductDto;
import ru.practicum.shopping.store.model.Product;

@Mapper(
        componentModel = "spring",
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
)
public interface ProductMapper {

    @Mapping(target = "quantityState", source = "quantityState")
    @Mapping(target = "productState", source = "productState")
    @Mapping(target = "productCategory", source = "productCategory")
    ProductDto toDto(Product product);

    @Mapping(target = "quantityState", source = "quantityState")
    @Mapping(target = "productState", source = "productState")
    @Mapping(target = "productCategory", source = "productCategory")
    Product toEntity(ProductDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "productId", ignore = true)
    @Mapping(target = "quantityState", source = "quantityState")
    @Mapping(target = "productState", source = "productState")
    @Mapping(target = "productCategory", source = "productCategory")
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
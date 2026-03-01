package ru.yandex.practicum.mapper;

import org.mapstruct.Mapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import ru.yandex.practicum.model.Product;
import ru.yandex.practicum.model.product.ProductDto;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductMapper {

    @Mapping(source = "productId", target = "productId")
    @Mapping(source = "productName", target = "productName")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "imageUrl", target = "imageSrc")
    @Mapping(source = "productStatus", target = "productStatus")
    @Mapping(source = "category", target = "productCategory")
    @Mapping(source = "price", target = "price")
    @Mapping(target = "quantityState", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Product toEntity(ProductDto productDto);

    @Mapping(source = "productId", target = "productId")
    @Mapping(source = "productName", target = "productName")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "imageSrc", target = "imageUrl")
    @Mapping(source = "productStatus", target = "productStatus")
    @Mapping(source = "productCategory", target = "category")
    @Mapping(source = "price", target = "price")
    ProductDto toDto(Product product);
}

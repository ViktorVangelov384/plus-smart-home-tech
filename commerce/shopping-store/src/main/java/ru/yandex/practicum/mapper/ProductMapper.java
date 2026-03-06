package ru.yandex.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import ru.yandex.practicum.model.Product;
import ru.yandex.practicum.model.product.ProductDto;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper {

    ProductDto toDto(Product product);

    Product toEntity(ProductDto productDto);

}

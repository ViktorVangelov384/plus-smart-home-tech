package ru.yandex.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import ru.yandex.practicum.model.WarehouseProduct;
import ru.yandex.practicum.model.warehouse.BookedProductsDto;
import ru.yandex.practicum.model.warehouse.RegisterProductInWarehouseRequest;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface WarehouseMapper {

    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "width", source = "dimensions.width")
    @Mapping(target = "height", source = "dimensions.height")
    @Mapping(target = "depth", source = "dimensions.depth")
    @Mapping(target = "weight", source = "weight")
    @Mapping(target = "fragile", source = "fragile")
    @Mapping(target = "quantity", constant = "0L")
    WarehouseProduct toEntity(RegisterProductInWarehouseRequest request);

    @Mapping(target = "totalWeight", source = "weight")
    @Mapping(target = "totalVolume", source = ".", qualifiedByName = "calculateVolume")
    @Mapping(target = "containsFragile", source = "fragile")
    BookedProductsDto toBookedProductsDto(WarehouseProduct product);

    @Named("calculateVolume")
    default Double calculateVolume(WarehouseProduct product) {
        if (product == null) return 0.0;
        return product.getWidth() * product.getHeight() * product.getDepth();
    }

}

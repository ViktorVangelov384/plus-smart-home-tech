package ru.yandex.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.yandex.practicum.model.ShoppingCart;
import ru.yandex.practicum.model.warehouse.ShoppingCartDto;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ShoppingCartMapper {

    @Mapping(target = "products", ignore = true)
    ShoppingCartDto toDto(ShoppingCart cart);

    @Mapping(target = "products", ignore = true)
    ShoppingCart toEntity(ShoppingCartDto cartDto);

}

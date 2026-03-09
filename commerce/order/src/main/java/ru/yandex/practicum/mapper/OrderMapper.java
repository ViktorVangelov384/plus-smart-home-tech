package ru.yandex.practicum.mapper;

import org.mapstruct.Mapper;
import ru.yandex.practicum.enums.OrderState;
import ru.yandex.practicum.model.Order;
import ru.yandex.practicum.model.order.OrderDto;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring", imports = {UUID.class, OrderState.class})
public interface OrderMapper {

    OrderDto toDto(Order order);

    List<OrderDto> toDtoList(List<Order> orders);
}

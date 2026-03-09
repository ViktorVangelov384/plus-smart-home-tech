package ru.yandex.practicum.service;

import ru.yandex.practicum.enums.OrderState;
import ru.yandex.practicum.model.order.OrderDto;
import ru.yandex.practicum.model.order.OrderRequest;
import ru.yandex.practicum.model.order.ProductResponse;

import java.util.List;
import java.util.UUID;

public interface OrderService {
    List<OrderDto> getUsersOrders(String userName);

    OrderDto createOrder(OrderRequest request);

    OrderDto returnOrder(ProductResponse response);

    OrderDto payOrderSuccess(UUID orderId);

    OrderDto changeOrderState(UUID orderId, OrderState state);

    OrderDto calculateTotalCost(UUID orderId);

    OrderDto calculateDeliveryCost(UUID orderId);
}

package ru.yandex.practicum.service;

import ru.yandex.practicum.model.delivery.DeliveryDto;
import ru.yandex.practicum.model.order.OrderDto;

import java.math.BigDecimal;
import java.util.UUID;

public interface DeliveryService {

    DeliveryDto createDelivery(DeliveryDto request);

    void markDeliveryAsDelivered(UUID orderId);

    void markDeliveryAsInProgress(UUID orderId);

    void markDeliveryAsFailed(UUID orderId);

    BigDecimal calculateDeliveryPrice(OrderDto orderDto);
}

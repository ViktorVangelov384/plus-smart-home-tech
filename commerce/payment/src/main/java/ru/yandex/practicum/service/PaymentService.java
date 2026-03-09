package ru.yandex.practicum.service;

import ru.yandex.practicum.model.order.OrderDto;
import ru.yandex.practicum.model.payment.PaymentDto;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentService {
    PaymentDto createPayment(OrderDto orderDto);

    BigDecimal calculateTotalCost(OrderDto orderDto);

    void confirmPayment(UUID paymentId);

    BigDecimal calculateProductCost(OrderDto orderDto);

    void processFailedPayment(UUID paymentId);
}

package ru.yandex.practicum.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.model.order.OrderDto;
import ru.yandex.practicum.model.payment.PaymentDto;

import java.math.BigDecimal;

@FeignClient(name = "payment")
public interface PaymentClient {

    @PostMapping("/api/v1/payment/product-cost")
    BigDecimal calculateProductCost(@RequestBody OrderDto orderDto);

    @PostMapping("/api/v1/payment/total-cost")
    BigDecimal calculateTotalCost(@RequestBody OrderDto orderDto);

    @PostMapping("/api/v1/payment")
    PaymentDto createPayment(@RequestBody OrderDto orderDto);
}

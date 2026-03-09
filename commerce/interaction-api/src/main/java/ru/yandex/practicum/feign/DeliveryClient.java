package ru.yandex.practicum.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.model.delivery.CreateDeliveryRequest;
import ru.yandex.practicum.model.delivery.DeliveryCostRequest;

import java.math.BigDecimal;
import java.util.UUID;

@FeignClient(name = "delivery")
public interface DeliveryClient {

    @PostMapping("/api/v1/delivery/plan")
    UUID createDelivery(@RequestBody CreateDeliveryRequest request);

    @PostMapping("/api/v1/delivery/cost")
    BigDecimal calculateDeliveryCost(@RequestBody DeliveryCostRequest request);
}

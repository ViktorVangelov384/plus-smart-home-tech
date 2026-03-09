package ru.yandex.practicum.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import java.util.UUID;

@FeignClient(name = "order")
public interface OrderClient {

    @PostMapping("/api/v1/order/payment/success/{orderId}")
    void notifyPaymentSuccess(@PathVariable("orderId") UUID orderId);

    @PostMapping("/api/v1/order/payment/failed/{orderId}")
    void notifyPaymentFailure(@PathVariable("orderId") UUID orderId);

    @PostMapping("/api/v1/order/{orderId}/delivery/success")
    void deliveryCompleted(@PathVariable("orderId") UUID orderId);

    @PostMapping("/api/v1/order/{orderId}/delivery/failed")
    void deliveryFailed(@PathVariable("orderId") UUID orderId);

    @PostMapping("/api/v1/order/{orderId}/assemble")
    void assembleOrder(@PathVariable("orderId") UUID orderId);

}

package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.model.delivery.DeliveryDto;
import ru.yandex.practicum.model.order.OrderDto;
import ru.yandex.practicum.service.DeliveryService;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/delivery")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    @PostMapping
    public ResponseEntity<DeliveryDto> createDelivery(@Valid @RequestBody DeliveryDto request) {
        log.info("Запрос на создание доставки для заказа: {}", request.getOrderId());
        DeliveryDto createdDelivery = deliveryService.createDelivery(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDelivery);
    }

    @PostMapping("/cost")
    public ResponseEntity<BigDecimal> calculateDeliveryPrice(@Valid @RequestBody OrderDto orderDto) {
        log.info("Запрос на расчет стоимости доставки для заказа: {}", orderDto.getOrderId());
        BigDecimal deliveryPrice = deliveryService.calculateDeliveryPrice(orderDto);
        return ResponseEntity.ok(deliveryPrice);
    }

    @PostMapping("/{orderId}/delivered")
    public ResponseEntity<Void> markDeliveryAsDelivered(@PathVariable UUID orderId) {
        log.info("Запрос на отметку доставки как выполненной для заказа: {}", orderId);
        deliveryService.markDeliveryAsDelivered(orderId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{orderId}/in-progress")
    public ResponseEntity<Void> markDeliveryAsInProgress(@PathVariable UUID orderId) {
        log.info("Запрос на отметку доставки как в процессе выполнения для заказа: {}", orderId);
        deliveryService.markDeliveryAsInProgress(orderId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{orderId}/failed")
    public ResponseEntity<Void> markDeliveryAsFailed(@PathVariable UUID orderId) {
        log.info("Запрос на отметку доставки как неудачной для заказа: {}", orderId);
        deliveryService.markDeliveryAsFailed(orderId);
        return ResponseEntity.ok().build();
    }
}

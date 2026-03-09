package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.enums.OrderState;
import ru.yandex.practicum.model.order.OrderDto;
import ru.yandex.practicum.model.order.OrderRequest;
import ru.yandex.practicum.model.order.ProductResponse;
import ru.yandex.practicum.service.OrderService;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<List<OrderDto>> getUsersOrders(@RequestParam String username) {
        log.info("Запрос списка заказов пользователя: {}", username);
        List<OrderDto> orders = orderService.getUsersOrders(username);
        return ResponseEntity.ok(orders);
    }

    @PostMapping
    public ResponseEntity<OrderDto> createOrder(@Valid @RequestBody OrderRequest request) {
        log.info("Запрос на создание заказа для пользователя: {}", request.getUsername());
        OrderDto createdOrder = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }

    @PostMapping("/{orderId}/payment/success")
    public ResponseEntity<OrderDto> payOrderSuccess(@PathVariable UUID orderId) {
        log.info("Запрос на подтверждение оплаты заказа: {}", orderId);
        OrderDto order = orderService.payOrderSuccess(orderId);
        return ResponseEntity.ok(order);
    }

    @PostMapping("/{orderId}/payment/failed")
    public ResponseEntity<OrderDto> changeOrderStateToPaymentFailed(@PathVariable UUID orderId) {
        log.info("Запрос на отметку неудачной оплаты заказа: {}", orderId);
        OrderDto order = orderService.changeOrderState(orderId, OrderState.PAYMENT_FAILED);
        return ResponseEntity.ok(order);
    }

    @PostMapping("/{orderId}/delivery/failed")
    public ResponseEntity<OrderDto> changeOrderStateToDeliveryFailed(@PathVariable UUID orderId) {
        log.info("Запрос на отметку неудачной доставки заказа: {}", orderId);
        OrderDto order = orderService.changeOrderState(orderId, OrderState.DELIVERY_FAILED);
        return ResponseEntity.ok(order);
    }

    @PostMapping("/{orderId}/assembly/failed")
    public ResponseEntity<OrderDto> changeOrderStateToAssemblyFailed(@PathVariable UUID orderId) {
        log.info("Запрос на отметку неудачной сборки заказа: {}", orderId);
        OrderDto order = orderService.changeOrderState(orderId, OrderState.ASSEMBLY_FAILED);
        return ResponseEntity.ok(order);
    }

    @PostMapping("/{orderId}/calculate/total")
    public ResponseEntity<OrderDto> calculateTotalCost(@PathVariable UUID orderId) {
        log.info("Запрос на расчет полной стоимости заказа: {}", orderId);
        OrderDto order = orderService.calculateTotalCost(orderId);
        return ResponseEntity.ok(order);
    }

    @PostMapping("/{orderId}/calculate/delivery")
    public ResponseEntity<OrderDto> calculateDeliveryCost(@PathVariable UUID orderId) {
        log.info("Запрос на расчет стоимости доставки заказа: {}", orderId);
        OrderDto order = orderService.calculateDeliveryCost(orderId);
        return ResponseEntity.ok(order);
    }

    @PostMapping("/{orderId}/state")
    public ResponseEntity<OrderDto> changeOrderState(
            @PathVariable UUID orderId,
            @RequestParam OrderState state) {
        log.info("Запрос на изменение статуса заказа {} на {}", orderId, state);
        OrderDto order = orderService.changeOrderState(orderId, state);
        return ResponseEntity.ok(order);
    }

    @PostMapping("/return")
    public ResponseEntity<OrderDto> returnOrder(@Valid @RequestBody ProductResponse response) {
        log.info("Запрос на возврат заказа: {}", response.getOrderId());
        OrderDto order = orderService.returnOrder(response);
        return ResponseEntity.ok(order);
    }

    @PostMapping("/{orderId}/assemble")
    public ResponseEntity<OrderDto> assembleOrder(@PathVariable UUID orderId) {
        log.info("Запрос на сборку заказа: {}", orderId);
        OrderDto order = orderService.changeOrderState(orderId, OrderState.ASSEMBLED);
        return ResponseEntity.ok(order);
    }

    @PostMapping("/{orderId}/ship")
    public ResponseEntity<OrderDto> shipOrder(@PathVariable UUID orderId) {
        log.info("Запрос на отгрузку заказа: {}", orderId);
        OrderDto order = orderService.changeOrderState(orderId, OrderState.ON_DELIVERY);
        return ResponseEntity.ok(order);
    }

    @PostMapping("/{orderId}/deliver")
    public ResponseEntity<OrderDto> deliverOrder(@PathVariable UUID orderId) {
        log.info("Запрос на доставку заказа: {}", orderId);
        OrderDto order = orderService.changeOrderState(orderId, OrderState.DELIVERED);
        return ResponseEntity.ok(order);
    }

    @PostMapping("/{orderId}/complete")
    public ResponseEntity<OrderDto> completeOrder(@PathVariable UUID orderId) {
        log.info("Запрос на завершение заказа: {}", orderId);
        OrderDto order = orderService.changeOrderState(orderId, OrderState.COMPLETED);
        return ResponseEntity.ok(order);
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderDto> cancelOrder(@PathVariable UUID orderId) {
        log.info("Запрос на отмену заказа: {}", orderId);
        OrderDto order = orderService.changeOrderState(orderId, OrderState.CANCELED);
        return ResponseEntity.ok(order);
    }
}
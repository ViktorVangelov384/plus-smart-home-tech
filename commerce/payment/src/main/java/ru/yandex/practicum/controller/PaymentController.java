package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.model.order.OrderDto;
import ru.yandex.practicum.model.payment.PaymentDto;
import ru.yandex.practicum.service.PaymentService;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/product-cost")
    public ResponseEntity<BigDecimal> calculateProductCost(@Valid @RequestBody OrderDto orderDto) {
        log.info("Запрос на расчет стоимости товаров для заказа: {}", orderDto.getOrderId());
        BigDecimal productCost = paymentService.calculateProductCost(orderDto);
        log.info("Стоимость товаров для заказа {}: {}", orderDto.getOrderId(), productCost);
        return ResponseEntity.ok(productCost);
    }

    @PostMapping("/total-cost")
    public ResponseEntity<BigDecimal> calculateTotalCost(@Valid @RequestBody OrderDto orderDto) {
        log.info("Запрос на расчет полной стоимости заказа: {}", orderDto.getOrderId());
        BigDecimal totalCost = paymentService.calculateTotalCost(orderDto);
        log.info("Полная стоимость заказа {}: {}", orderDto.getOrderId(), totalCost);
        return ResponseEntity.ok(totalCost);
    }

    @PostMapping
    public ResponseEntity<PaymentDto> createPayment(@Valid @RequestBody OrderDto orderDto) {
        log.info("Запрос на создание платежа для заказа: {}", orderDto.getOrderId());
        PaymentDto createdPayment = paymentService.createPayment(orderDto);
        log.info("Платеж {} создан для заказа {}", createdPayment.getPaymentId(), orderDto.getOrderId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPayment);
    }

    @PostMapping("/{paymentId}/confirm")
    public ResponseEntity<Void> confirmPayment(@PathVariable UUID paymentId) {
        log.info("Запрос на подтверждение платежа: {}", paymentId);
        paymentService.confirmPayment(paymentId);
        log.info("Платеж {} подтвержден", paymentId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{paymentId}/fail")
    public ResponseEntity<Void> processFailedPayment(@PathVariable UUID paymentId) {
        log.info("Запрос на обработку неудачного платежа: {}", paymentId);
        paymentService.processFailedPayment(paymentId);
        log.info("Платеж {} отмечен как неудачный", paymentId);
        return ResponseEntity.ok().build();
    }
}
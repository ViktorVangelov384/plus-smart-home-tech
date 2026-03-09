package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.calculator.PaymentCalculator;
import ru.yandex.practicum.feign.OrderClient;
import ru.yandex.practicum.model.order.OrderDto;
import ru.yandex.practicum.model.payment.PaymentDto;
import ru.yandex.practicum.exception.PaymentAlreadyExistsException;
import ru.yandex.practicum.exception.PaymentNotFoundException;
import ru.yandex.practicum.mapper.PaymentMapper;
import ru.yandex.practicum.model.Payment;
import ru.yandex.practicum.enums.PaymentStatus;
import ru.yandex.practicum.repository.PaymentRepository;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final OrderClient orderClient;
    private final PaymentCalculator paymentCalculator;

    private static final String PAYMENT_NOT_FOUND_MSG = "Платеж с ID %s не найден";
    private static final String PAYMENT_EXISTS_MSG = "Платеж для заказа %s уже существует";

    @Override
    public BigDecimal calculateProductCost(OrderDto orderDto) {
        log.debug("Расчет стоимости товаров для заказа: {}", orderDto.getOrderId());

        BigDecimal productCost = paymentCalculator.calculateProductsTotal(orderDto);
        log.info("Стоимость товаров для заказа {}: {}", orderDto.getOrderId(), productCost);

        return productCost;
    }

    @Override
    public BigDecimal calculateTotalCost(OrderDto orderDto) {
        log.debug("Расчет полной стоимости заказа: {}", orderDto.getOrderId());

        BigDecimal totalCost = paymentCalculator.calculateOrderTotal(orderDto);
        log.info("Полная стоимость заказа {}: {}", orderDto.getOrderId(), totalCost);

        return totalCost;
    }

    @Transactional
    @Override
    public PaymentDto createPayment(OrderDto orderDto) {
        log.info("Создание платежа для заказа: {}", orderDto.getOrderId());

        checkPaymentNotExists(orderDto.getOrderId());

        Payment payment = buildPaymentFromOrder(orderDto);
        Payment savedPayment = paymentRepository.save(payment);

        log.info("Платеж {} успешно создан для заказа {}",
                savedPayment.getPaymentId(), orderDto.getOrderId());

        return paymentMapper.toDto(savedPayment);
    }

    @Transactional
    @Override
    public void confirmPayment(UUID paymentId) {
        log.info("Обработка успешного платежа: {}", paymentId);

        Payment payment = findPaymentById(paymentId);
        payment.setStatus(PaymentStatus.SUCCESS);

        paymentRepository.save(payment);
        orderClient.notifyPaymentSuccess(payment.getOrderId());

        log.info("Платеж {} подтвержден, статус заказа {} обновлен",
                paymentId, payment.getOrderId());
    }

    @Transactional
    @Override
    public void processFailedPayment(UUID paymentId) {
        log.info("Обработка неудачного платежа: {}", paymentId);

        Payment payment = findPaymentById(paymentId);
        payment.setStatus(PaymentStatus.FAILED);

        paymentRepository.save(payment);
        orderClient.notifyPaymentFailure(payment.getOrderId());

        log.info("Платеж {} отменен, статус заказа {} обновлен",
                paymentId, payment.getOrderId());
    }

    private Payment findPaymentById(UUID paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> {
                    log.error("Платеж не найден: {}", paymentId);
                    return new PaymentNotFoundException(
                            String.format(PAYMENT_NOT_FOUND_MSG, paymentId));
                });
    }

    private void checkPaymentNotExists(UUID orderId) {
        if (paymentRepository.existsByOrderId(orderId)) {
            log.error("Платеж для заказа {} уже существует", orderId);
            throw new PaymentAlreadyExistsException(
                    String.format(PAYMENT_EXISTS_MSG, orderId));
        }
    }

    private Payment buildPaymentFromOrder(OrderDto orderDto) {
        BigDecimal fee = calculateFee(orderDto);

        return Payment.builder()
                .paymentId(UUID.randomUUID())
                .orderId(orderDto.getOrderId())
                .deliveryTotal(orderDto.getDeliveryPrice())
                .totalPayment(orderDto.getTotalPrice())
                .feeTotal(fee)
                .status(PaymentStatus.PENDING)
                .build();
    }

    private BigDecimal calculateFee(OrderDto orderDto) {
        return orderDto.getTotalPrice()
                .subtract(orderDto.getProductPrice())
                .subtract(orderDto.getDeliveryPrice());
    }
}
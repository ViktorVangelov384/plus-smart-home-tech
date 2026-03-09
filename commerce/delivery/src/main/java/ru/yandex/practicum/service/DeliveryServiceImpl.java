package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.calculator.DeliveryCalculator;
import ru.yandex.practicum.enums.DeliveryState;
import ru.yandex.practicum.exception.DeliveryNotFoundException;
import ru.yandex.practicum.exception.DuplicateDeliveryException;
import ru.yandex.practicum.feign.OrderClient;
import ru.yandex.practicum.mapper.DeliveryMapper;
import ru.yandex.practicum.model.Delivery;
import ru.yandex.practicum.model.delivery.DeliveryDto;
import ru.yandex.practicum.model.order.OrderDto;
import ru.yandex.practicum.repository.DeliveryRepository;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final DeliveryMapper deliveryMapper;
    private final OrderClient orderClient;
    private final DeliveryCalculator deliveryCalculator;

    private static final String DELIVERY_NOT_FOUND_MSG = "Доставка для заказа %s не найдена";
    private static final String DELIVERY_EXISTS_MSG = "Доставка для заказа %s уже существует";

    @Transactional
    @Override
    public DeliveryDto createDelivery(DeliveryDto request) {
        log.info("Создание доставки для заказа: {}", request.getOrderId());

        if (deliveryRepository.existsByOrderId(request.getOrderId())) {
            log.error("Доставка для заказа {} уже существует", request.getOrderId());
            throw new DuplicateDeliveryException(
                    String.format(DELIVERY_EXISTS_MSG, request.getOrderId()));
        }

        Delivery delivery = new Delivery();
        delivery.setDeliveryId(UUID.randomUUID());
        delivery.setOrderId(request.getOrderId());
        delivery.setDeliveryState(DeliveryState.CREATED);

        delivery.setCountryFrom(request.getFromAddress().getCountry());
        delivery.setCityFrom(request.getFromAddress().getCity());
        delivery.setStreetFrom(request.getFromAddress().getStreet());
        delivery.setHouseFrom(request.getFromAddress().getHouse());
        delivery.setFlatFrom(request.getFromAddress().getFlat());

        delivery.setCountryTo(request.getToAddress().getCountry());
        delivery.setCityTo(request.getToAddress().getCity());
        delivery.setStreetTo(request.getToAddress().getStreet());
        delivery.setHouseTo(request.getToAddress().getHouse());
        delivery.setFlatTo(request.getToAddress().getFlat());

        delivery.setTotalWeight(request.getTotalWeight());
        delivery.setTotalVolume(request.getTotalVolume());
        delivery.setFragile(request.getFragile());
        delivery.setDeliveryCost(request.getDeliveryCost());

        Delivery savedDelivery = deliveryRepository.save(delivery);
        log.info("Доставка {} создана для заказа {}",
                savedDelivery.getDeliveryId(), request.getOrderId());

        return deliveryMapper.toDto(savedDelivery);
    }

    @Override
    public BigDecimal calculateDeliveryPrice(OrderDto orderDto) {
        log.info("Расчет стоимости доставки для заказа: {}", orderDto.getOrderId());

        BigDecimal price = deliveryCalculator.calculateDeliveryPrice(orderDto);

        // Сохраняем цену в доставку
        Delivery delivery = findDeliveryByOrderId(orderDto.getOrderId());
        delivery.setDeliveryCost(price);
        deliveryRepository.save(delivery);

        return price;
    }

    @Transactional
    @Override
    public void markDeliveryAsInProgress(UUID orderId) {
        log.info("Заказ {} передан в доставку", orderId);

        Delivery delivery = findDeliveryByOrderId(orderId);
        delivery.setDeliveryState(DeliveryState.IN_PROGRESS);

        orderClient.assembleOrder(orderId);
        deliveryRepository.save(delivery);

        log.info("Заказ {} в доставке", orderId);
    }

    @Transactional
    @Override
    public void markDeliveryAsDelivered(UUID orderId) {
        log.info("Заказ {} доставлен", orderId);

        Delivery delivery = findDeliveryByOrderId(orderId);
        delivery.setDeliveryState(DeliveryState.DELIVERED);

        orderClient.deliveryCompleted(orderId);
        deliveryRepository.save(delivery);

        log.info("Заказ {} успешно доставлен", orderId);
    }

    @Transactional
    @Override
    public void markDeliveryAsFailed(UUID orderId) {
        log.info("Ошибка доставки заказа {}", orderId);

        Delivery delivery = findDeliveryByOrderId(orderId);
        delivery.setDeliveryState(DeliveryState.FAILED);

        orderClient.deliveryFailed(orderId);
        deliveryRepository.save(delivery);

        log.info("Доставка заказа {} отмечена как неудачная", orderId);
    }

    private Delivery findDeliveryByOrderId(UUID orderId) {
        return deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> {
                    log.error("Доставка для заказа {} не найдена", orderId);
                    return new DeliveryNotFoundException(
                            String.format(DELIVERY_NOT_FOUND_MSG, orderId));
                });
    }
}
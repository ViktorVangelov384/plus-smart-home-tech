package ru.yandex.practicum.calculator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.model.order.OrderDto;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Component
public class DeliveryCalculator {

    private static final BigDecimal BASE_RATE = BigDecimal.valueOf(5.0);
    private static final BigDecimal FRAGILE_MULTIPLIER = BigDecimal.valueOf(0.2);
    private static final BigDecimal WEIGHT_MULTIPLIER = BigDecimal.valueOf(0.3);
    private static final BigDecimal VOLUME_MULTIPLIER = BigDecimal.valueOf(0.2);

    public BigDecimal calculateDeliveryPrice(OrderDto orderDto) {
        log.debug("Расчет стоимости доставки для заказа: {}", orderDto.getOrderId());

        BigDecimal total = BASE_RATE;
        log.debug("Базовая ставка: {}", total);

        if (orderDto.isFragile()) {
            BigDecimal fragileCost = total.multiply(FRAGILE_MULTIPLIER);
            total = total.add(fragileCost);
            log.debug("После учета хрупкости (+{}): {}", fragileCost, total);
        }

        if (orderDto.getDeliveryWeight() != null) {
            BigDecimal weightCost = orderDto.getDeliveryWeight()
                    .multiply(WEIGHT_MULTIPLIER);
            total = total.add(weightCost);
            log.debug("После учета веса {} * {} = +{}: {}",
                    orderDto.getDeliveryWeight(), WEIGHT_MULTIPLIER, weightCost, total);
        }

        if (orderDto.getDeliveryVolume() != null) {
            BigDecimal volumeCost = orderDto.getDeliveryVolume()
                    .multiply(VOLUME_MULTIPLIER);
            total = total.add(volumeCost);
            log.debug("После учета объема {} * {} = +{}: {}",
                    orderDto.getDeliveryVolume(), VOLUME_MULTIPLIER, volumeCost, total);
        }

        total = total.setScale(2, RoundingMode.HALF_UP);
        log.info("Итоговая стоимость доставки для заказа {}: {}",
                orderDto.getOrderId(), total);

        return total;
    }
}
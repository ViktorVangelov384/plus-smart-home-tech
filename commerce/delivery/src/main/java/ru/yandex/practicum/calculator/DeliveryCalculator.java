package ru.yandex.practicum.calculator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.model.order.OrderDto;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Component
public class DeliveryCalculator {

    private final BigDecimal baseRate;
    private final BigDecimal fragileMultiplier;
    private final BigDecimal weightMultiplier;
    private final BigDecimal volumeMultiplier;

    public DeliveryCalculator(
            @Value("${delivery.calculation.base-rate:5.0}") BigDecimal baseRate,
            @Value("${delivery.calculation.fragile-multiplier:0.2}") BigDecimal fragileMultiplier,
            @Value("${delivery.calculation.weight-multiplier:0.3}") BigDecimal weightMultiplier,
            @Value("${delivery.calculation.volume-multiplier:0.2}") BigDecimal volumeMultiplier) {
        this.baseRate = baseRate;
        this.fragileMultiplier = fragileMultiplier;
        this.weightMultiplier = weightMultiplier;
        this.volumeMultiplier = volumeMultiplier;
    }

    public BigDecimal calculateDeliveryPrice(OrderDto orderDto) {
        log.debug("Расчет стоимости доставки для заказа: {}", orderDto.getOrderId());

        BigDecimal total = baseRate;
        log.debug("Базовая ставка: {}", total);

        if (orderDto.isFragile()) {
            BigDecimal fragileCost = total.multiply(fragileMultiplier);
            total = total.add(fragileCost);
        }

        if (orderDto.getDeliveryWeight() != null) {
            BigDecimal weightCost = orderDto.getDeliveryWeight()
                    .multiply(weightMultiplier);
            total = total.add(weightCost);
        }

        if (orderDto.getDeliveryVolume() != null) {
            BigDecimal volumeCost = orderDto.getDeliveryVolume()
                    .multiply(volumeMultiplier);
            total = total.add(volumeCost);
        }

        total = total.setScale(2, RoundingMode.HALF_UP);
        log.info("Итоговая стоимость доставки для заказа {}: {}",
                orderDto.getOrderId(), total);

        return total;
    }
}
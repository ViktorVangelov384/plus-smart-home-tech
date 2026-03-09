package ru.yandex.practicum.calculator;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.feign.ShoppingStoreClient;
import ru.yandex.practicum.model.order.OrderDto;
import ru.yandex.practicum.model.product.ProductDto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaymentCalculator {

    private final ShoppingStoreClient shoppingStoreClient;

    @Value("${payment.tax.rate:0.1}")
    private BigDecimal taxRate;

    public BigDecimal calculateProductsTotal(OrderDto orderDto) {
        BigDecimal total = BigDecimal.ZERO;

        for (Map.Entry<UUID, Long> entry : orderDto.getProducts().entrySet()) {
            UUID productId = entry.getKey();
            Long quantity = entry.getValue();

            ProductDto product = shoppingStoreClient.getProduct(productId);
            BigDecimal itemTotal = product.getPrice()
                    .multiply(BigDecimal.valueOf(quantity));
            total = total.add(itemTotal);
        }

        return total;
    }

    public BigDecimal calculateOrderTotal(OrderDto orderDto) {
         BigDecimal productCost = orderDto.getProductPrice() != null
                ? orderDto.getProductPrice()
                : calculateProductsTotal(orderDto);

        BigDecimal deliveryCost = orderDto.getDeliveryPrice() != null
                ? orderDto.getDeliveryPrice()
                : BigDecimal.ZERO;

        BigDecimal tax = productCost.multiply(taxRate)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal productWithTax = productCost.add(tax);

        BigDecimal total = productWithTax.add(deliveryCost)
                .setScale(2, RoundingMode.HALF_UP);

        return total;
    }
}

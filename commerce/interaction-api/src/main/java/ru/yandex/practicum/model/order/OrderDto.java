package ru.yandex.practicum.model.order;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.yandex.practicum.enums.OrderState;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class OrderDto {
    @NotNull
    private UUID orderId;
    private UUID shoppingCartId;

    @NotEmpty
    private Map<UUID, Long> products;
    private UUID paymentId;
    private UUID deliveryId;
    private OrderState state;


    private BigDecimal deliveryWeight;
    private BigDecimal deliveryVolume;
    private boolean fragile;

    @NotNull
    @Positive
    private BigDecimal totalPrice;

    @NotNull
    @Positive
    private BigDecimal deliveryPrice;

    @NotNull
    @Positive
    private BigDecimal productPrice;

    @NotNull
    private String deliveryStreet;
}

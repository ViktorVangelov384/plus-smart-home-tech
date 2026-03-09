package ru.yandex.practicum.model.delivery;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import ru.yandex.practicum.enums.DeliveryState;
import ru.yandex.practicum.model.warehouse.AddressDto;

import java.math.BigDecimal;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
@Builder
@AllArgsConstructor
public class DeliveryDto {
    private UUID deliveryId;

    @NotNull(message = "Не указан адрес отправителя")
    @Valid
    private AddressDto fromAddress;

    @NotNull(message = "Не указан адрес доставки")
    @Valid
    private AddressDto toAddress;

    @NotNull(message = "Не указан Id заказа")
    private UUID orderId;

    @NotNull(message = "Не указан статус доставки")
    private DeliveryState deliveryState;

    @NotNull
    @Positive
    private BigDecimal totalVolume;

    @NotNull
    @Positive
    private BigDecimal totalWeight;

    private Boolean fragile;

    @NotNull
    @PositiveOrZero
    private BigDecimal deliveryCost;
}

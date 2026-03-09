package ru.yandex.practicum.model.order;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class ProductResponse {
    @NotNull(message = "Номер заказа не указан!")
    private UUID orderId;

    @NotNull(message = "Нет продуктов в заказе")
    private Map<UUID, Long> products;
}

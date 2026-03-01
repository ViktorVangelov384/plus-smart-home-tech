package ru.yandex.practicum.model.cart;

import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
public class ShoppingCartDto {
    private UUID cartId;
    private Map<UUID, Long> products;
}

package ru.yandex.practicum.model.warehouse;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
public class ShoppingCartDto {

    @NotNull
    private UUID cartId;

    @NotEmpty
    private Map<UUID, Long> products;
}

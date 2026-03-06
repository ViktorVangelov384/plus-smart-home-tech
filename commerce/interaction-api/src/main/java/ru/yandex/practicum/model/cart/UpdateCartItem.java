package ru.yandex.practicum.model.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class UpdateCartItem {

    @NotNull
    private UUID productId;

    @NotNull
    @Min(value = 1)
    private Long newQuantity;
}

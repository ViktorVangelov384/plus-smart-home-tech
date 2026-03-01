package ru.yandex.practicum.model.warehouse;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class RegisterProductInWarehouseRequest {

    @NotNull
    private UUID productId;

    private Boolean fragile;

    @NotNull
    private ProductDimensionDto dimension;

    @NotNull
    @Min(1)
    private Double weight;
}

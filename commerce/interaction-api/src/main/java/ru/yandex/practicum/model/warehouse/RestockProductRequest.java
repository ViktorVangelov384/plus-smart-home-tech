package ru.yandex.practicum.model.warehouse;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class RestockProductRequest {

    @NotNull
    private UUID productId;

    @NotNull
    @Min(1)
    private Long quantity;
}
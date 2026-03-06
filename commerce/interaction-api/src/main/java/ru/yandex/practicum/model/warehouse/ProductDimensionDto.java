package ru.yandex.practicum.model.warehouse;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProductDimensionDto {

    @NotNull
    @Min(1)
    private Double width;

    @NotNull
    @Min(1)
    private Double height;

    @NotNull
    @Min(1)
    private Double depth;
}

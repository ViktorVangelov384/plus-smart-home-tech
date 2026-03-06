package ru.yandex.practicum.model.warehouse;

import lombok.Data;

@Data
public class BookedProductsDto {

    private Double totalWeight;
    private Double totalVolume;
    private Boolean containsFragile;
}

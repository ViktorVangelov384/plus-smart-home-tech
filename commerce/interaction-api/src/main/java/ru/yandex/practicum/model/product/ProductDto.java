package ru.yandex.practicum.model.product;

import jakarta.validation.constraints.*;
import lombok.Data;
import ru.yandex.practicum.enums.ProductCategory;
import ru.yandex.practicum.enums.ProductStatus;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class ProductDto {

    @NotNull
    private UUID productId;

    @NotBlank
    private String productName;

    @NotBlank
    private String description;

    private String imageUrl;

    @NotNull
    private ProductStatus productStatus;

    @NotNull
    private ProductCategory category;

    @NotNull
    @Min(1)
    private BigDecimal price;
}

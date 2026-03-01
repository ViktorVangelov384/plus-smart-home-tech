package ru.yandex.practicum.model.product;

import jakarta.validation.constraints.*;
import lombok.Data;
import ru.yandex.practicum.enums.ProductCategory;
import ru.yandex.practicum.enums.ProductState;
import ru.yandex.practicum.enums.Quantity;

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

    private String imageSrc;

    @NotNull
    private Quantity quantityState;

    @NotNull
    private ProductState productState;

    @NotNull
    private ProductCategory productCategory;

    @NotNull
    @Min(1)
    private BigDecimal price;

}

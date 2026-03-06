package ru.yandex.practicum.model.warehouse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductStockResponse {
    private UUID productId;
    private Long requestedQuantity;
    private boolean available;
    private Long availableQuantity;
    private String message;

    public ProductStockResponse(UUID productId, Long requestedQuantity, boolean available) {
        this.productId = productId;
        this.requestedQuantity = requestedQuantity;
        this.available = available;
    }
}

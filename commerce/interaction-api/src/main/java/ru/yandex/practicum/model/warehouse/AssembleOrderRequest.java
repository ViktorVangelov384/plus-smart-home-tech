package ru.yandex.practicum.model.warehouse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssembleOrderRequest {
    private UUID orderId;
    private Map<UUID, Long> products;
}

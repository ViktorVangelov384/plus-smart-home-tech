package ru.practicum.hub.config;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TemperatureSensorConfig {
    @NotNull
    private String id;

    @NotNull
    private ValueRangeConfig temperature;
}

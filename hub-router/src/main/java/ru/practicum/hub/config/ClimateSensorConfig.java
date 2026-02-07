package ru.practicum.hub.config;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ClimateSensorConfig {

    @NotNull
    private String id;

    @NotNull
    private ValueRangeConfig temperature;

    @NotNull
    private ValueRangeConfig humidity;

    @NotNull
    private ValueRangeConfig co2Level;
}

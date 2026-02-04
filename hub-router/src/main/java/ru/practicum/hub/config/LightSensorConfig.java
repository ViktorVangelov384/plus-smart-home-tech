package ru.practicum.hub.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LightSensorConfig {

    @NotBlank
    String id;

    @Valid
    @NotNull
    ValueRangeConfig luminosity;

    @Valid
    @NotNull
    ValueRangeConfig linkQuality;

}
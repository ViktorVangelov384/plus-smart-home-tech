package ru.practicum.hub.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MotionSensorConfig {

    @NotBlank
    String id;

    @Valid
    @NotNull
    ValueRangeConfig linkQuality;

    @Valid
    @NotNull
    ValueRangeConfig voltage;
}

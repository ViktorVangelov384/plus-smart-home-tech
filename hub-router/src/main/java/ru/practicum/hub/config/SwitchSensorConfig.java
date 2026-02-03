package ru.practicum.hub.config;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SwitchSensorConfig {

    @NotNull
    private String id;
}

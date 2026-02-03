package ru.practicum.hub.config;

import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "sensor")
public class SensorConfig {

    private List<@Valid MotionSensorConfig> motionSensors;
    private List<@Valid SwitchSensorConfig> switchSensors;
    private List<@Valid TemperatureSensorConfig> temperatureSensors;
    private List<@Valid LightSensorConfig> lightSensors;
    private List<@Valid ClimateSensorConfig> climateSensors;
}

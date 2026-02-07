package ru.yandex.practicum.dto.sensor;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.entity.SensorEventType;

@Getter
@Setter
@ToString(callSuper = true)

public class ClimateSensorEventDto extends SensorEventDto {

    @NotNull(message = "Температура обязательна")
    @Min(value = -50, message = "Температура не может быть ниже -50°C")
    @Max(value = 50, message = "Температура не может быть выше 60°C")
    private Integer temperatureC;

    @NotNull(message = "Влажность обязательна")
    @Min(value = 0, message = "Влажность не может быть отрицательной")
    @Max(value = 100, message = "Влажность не может превышать 100%")
    private Integer humidity;

    @NotNull(message = "Уровень CO2 обязателен")
    private Integer co2Level;

    @Override
    public SensorEventType getType() {
        return SensorEventType.CLIMATE_SENSOR_EVENT;
    }

    public boolean isComfortable() {
        return temperatureC != null && temperatureC >= 18 && temperatureC <= 24 &&
                humidity != null && humidity >= 30 && humidity <= 60 &&
                co2Level != null && co2Level <= 1000;
    }
}

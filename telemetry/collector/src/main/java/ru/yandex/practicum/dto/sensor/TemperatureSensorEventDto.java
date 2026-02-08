package ru.yandex.practicum.dto.sensor;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.enums.SensorEventType;

@Getter
@Setter
@ToString(callSuper = true)
public class TemperatureSensorEventDto extends SensorEventDto {

    @NotNull(message = "Температура в Цельсиях обязательна")
    @Min(value = -50, message = "Температура не может быть ниже -50°C")
    @Max(value = 50, message = "Температура не может быть выше 50°C")
    private Integer temperatureC;

    @Override
    public SensorEventType getType() {
        return SensorEventType.TEMPERATURE_SENSOR_EVENT;
    }

    public String getTemperatureDescription() {
        if (temperatureC == null) return "Неизвестно";
        if (temperatureC < 0) return "Мороз";
        if (temperatureC < 10) return "Холодно";
        if (temperatureC < 20) return "Прохладно";
        if (temperatureC < 25) return "Комфортно";
        if (temperatureC < 30) return "Тепло";
        return "Жарко";
    }

    public boolean isComfortableTemperature() {
        return temperatureC != null && temperatureC >= 18 && temperatureC <= 24;
    }
}

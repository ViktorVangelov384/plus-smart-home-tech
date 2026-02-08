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
public class LightSensorEventDto extends SensorEventDto {

    @NotNull(message = "Качество связи обязательно")
    private Integer linkQuality;

    @NotNull(message = "Освещенность обязательна")
    @Min(value = 0, message = "Освещенность не может быть отрицательной")
    @Max(value = 100000, message = "Освещенность не может превышать 100000 люкс")
    private Integer luminosity;

    @Override
    public SensorEventType getType() {
        return SensorEventType.LIGHT_SENSOR_EVENT;
    }

    public boolean isSufficientLight() {
        return luminosity != null && luminosity >= 300;
    }

    public boolean hasGoodConnection() {
        return linkQuality != null && linkQuality >= 50;
    }

    public String getLightLevelDescription() {
        if (luminosity == null) return "Неизвестно";
        if (luminosity < 50) return "Очень темно";
        if (luminosity < 300) return "Темно";
        if (luminosity < 1000) return "Средняя освещенность";
        if (luminosity < 5000) return "Ярко";
        return "Очень ярко";
    }
}

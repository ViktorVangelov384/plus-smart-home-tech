package ru.yandex.practicum.dto.sensor;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.enums.SensorEventType;

@Getter
@Setter
@ToString(callSuper = true)
public class MotionSensorEventDto extends SensorEventDto {

    @NotNull(message = "Качество связи обязательно")
    private Integer linkQuality;

    @NotNull(message = "Состояние движения обязательно")
    private Boolean motion;

    @NotNull(message = "Напряжение обязательно")
    private Integer voltage;

    @Override
    public SensorEventType getType() {
        return SensorEventType.MOTION_SENSOR_EVENT;
    }

    public boolean hasMotionDetected() {
        return Boolean.TRUE.equals(motion);
    }
}

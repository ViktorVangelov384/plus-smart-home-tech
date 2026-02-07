package ru.yandex.practicum.dto.sensor;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.entity.SensorEventType;

@Getter
@Setter
@ToString(callSuper = true)
public class SwitchSensorEventDto extends SensorEventDto {

    @NotNull(message = "Состояние переключателя обязательно")
    private Boolean state;

    @Override
    public SensorEventType getType() {
        return SensorEventType.SWITCH_SENSOR_EVENT;
    }

    public boolean isOn() {
        return Boolean.TRUE.equals(state);
    }

    public boolean isOff() {
        return Boolean.FALSE.equals(state);
    }

    public String getStateDescription() {
        return isOn() ? "Включено" : "Выключено";
    }
}

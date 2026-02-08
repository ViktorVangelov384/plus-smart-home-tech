package ru.yandex.practicum.mapper.sensor;

import ru.yandex.practicum.dto.sensor.SensorEventDto;
import ru.yandex.practicum.enums.SensorEventType;

public interface SensorEventHandler {

    boolean canHandle(SensorEventDto sensorEventDto);

    void handle(SensorEventDto sensorEventDto);

    SensorEventType getSupportedType();
}

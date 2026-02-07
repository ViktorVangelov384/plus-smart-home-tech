package ru.yandex.practicum.mapper.sensor;

import ru.yandex.practicum.dto.sensor.SensorEventDto;
import ru.yandex.practicum.entity.SensorEventType;

public interface SensorEventHandler {

    boolean canHandle(SensorEventDto sensorEventDto);

    void handle(SensorEventDto sensorEventDto);

    SensorEventType getSupportedType();
}

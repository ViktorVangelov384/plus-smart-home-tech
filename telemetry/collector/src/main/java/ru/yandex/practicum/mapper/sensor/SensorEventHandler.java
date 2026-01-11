package ru.yandex.practicum.mapper.sensor;

import ru.yandex.practicum.dto.sensor.SensorEventDto;

public interface SensorEventHandler {

    boolean canHandle(SensorEventDto sensorEventDto);

    void handle(SensorEventDto sensorEventDto);
}

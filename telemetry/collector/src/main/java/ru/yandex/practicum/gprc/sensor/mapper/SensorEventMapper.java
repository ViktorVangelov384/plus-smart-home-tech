package ru.yandex.practicum.gprc.sensor.mapper;

import ru.yandex.practicum.dto.sensor.SensorEventDto;
import ru.yandex.practicum.entity.SensorEventType;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;

public interface SensorEventMapper {

    SensorEventDto mapFromProto(SensorEventProto sensorEventProto);

    SensorEventType getSupportedType();

}

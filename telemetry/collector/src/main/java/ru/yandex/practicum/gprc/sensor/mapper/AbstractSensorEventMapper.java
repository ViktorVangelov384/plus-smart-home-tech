package ru.yandex.practicum.gprc.sensor.mapper;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.dto.sensor.SensorEventDto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;

import java.time.Instant;

@Slf4j
public abstract class AbstractSensorEventMapper<T extends SensorEventDto> implements SensorEventMapper {
    @Override
    public T mapFromProto(SensorEventProto sensorEventProto) {
        try {
            validateInput(sensorEventProto);
            validateSensorSpecificData(sensorEventProto);
            T sensorEventDto = createSensorEventDto();
            populateCommonFields(sensorEventDto, sensorEventProto);
            populateSensorSpecificFields(sensorEventDto, sensorEventProto);
            validateResult(sensorEventDto);
            return sensorEventDto;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(String.format("Ошибка преобразования protobuf → DTO: %s", e.getMessage()), e);
        }
    }

    protected abstract T createSensorEventDto();

    protected abstract void validateSensorSpecificData(SensorEventProto sensorEventProto);

    protected abstract void populateSensorSpecificFields(T sensorEventDto, SensorEventProto sensorEventProto);

    protected void validateInput(SensorEventProto sensorEventProto) {
        if (sensorEventProto == null) {
            throw new IllegalArgumentException("SensorEventProto не может быть null");
        }
        if (sensorEventProto.getId() == null || sensorEventProto.getId().isBlank()) {
            throw new IllegalArgumentException("Идентификатор события датчика не может быть пустым");
        }
        if (sensorEventProto.getHubId() == null || sensorEventProto.getHubId().isBlank()) {
            throw new IllegalArgumentException("Идентификатор хаба не может быть пустым");
        }
    }

    protected void populateCommonFields(T sensorEventDto, SensorEventProto sensorEventProto) {
        sensorEventDto.setId(sensorEventProto.getId());
        sensorEventDto.setHubId(sensorEventProto.getHubId());
        Instant timestamp = extractTimestamp(sensorEventProto);
        sensorEventDto.setTimestamp(timestamp);
        log.debug("Заполнены общие поля DTO: ID={}, Hub={}, timestamp={}", sensorEventProto.getId(), sensorEventProto.getHubId(), timestamp);
    }

    protected Instant extractTimestamp(SensorEventProto sensorEventProto) {
        if (sensorEventProto.hasTimestamp()) {
            com.google.protobuf.Timestamp timestamp = sensorEventProto.getTimestamp();
            Instant instant = Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
            log.debug("Использован timestamp из protobuf: {}", instant);
            return instant;
        } else {
            Instant now = Instant.now();
            log.debug("Timestamp не установлен в protobuf, используется текущее время: {}", now);
            return now;
        }
    }

    protected void validateResult(T sensorEventDto) {
        if (sensorEventDto.getId() == null || sensorEventDto.getId().isBlank()) {
            throw new IllegalStateException("ID события не может быть пустым после преобразования");
        }
        if (sensorEventDto.getHubId() == null || sensorEventDto.getHubId().isBlank()) {
            throw new IllegalStateException("Hub ID не может быть пустым после преобразования");
        }
        if (sensorEventDto.getTimestamp() == null) {
            throw new IllegalStateException("Timestamp не может быть null после преобразования");
        }
        if (sensorEventDto.getType() == null) {
            throw new IllegalStateException("Тип события не может быть null после преобразования");
        }
    }
}
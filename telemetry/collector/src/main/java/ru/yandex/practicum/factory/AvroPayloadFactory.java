package ru.yandex.practicum.factory;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecord;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.dto.sensor.*;
import ru.yandex.practicum.kafka.telemetry.event.*;

@Slf4j
@Component
public class AvroPayloadFactory {

    public SpecificRecord createAvroPayload(SensorEventDto dto) {
        log.debug("Создание Avro payload для типа: {}", dto.getType());

        try {
            SpecificRecord payload = createPayloadByType(dto);
            log.debug("Создан Avro payload: {}", payload.getClass().getSimpleName());
            return payload;

        } catch (ClassCastException e) {
            log.error("Ошибка приведения типа для DTO: {}", dto.getClass().getSimpleName(), e);
            throw new IllegalArgumentException("Неверный тип DTO: " + dto.getClass().getSimpleName(), e);
        }
    }

    private SpecificRecord createPayloadByType(SensorEventDto dto) {
        switch (dto.getType()) {
            case CLIMATE_SENSOR_EVENT:
                ClimateSensorEventDto climateDto = (ClimateSensorEventDto) dto;
                return ClimateSensorAvro.newBuilder()
                        .setTemperatureC(climateDto.getTemperatureC())
                        .setHumidity(climateDto.getHumidity())
                        .setCo2Level(climateDto.getCo2Level())
                        .build();

            case LIGHT_SENSOR_EVENT:
                LightSensorEventDto lightDto = (LightSensorEventDto) dto;
                return LightSensorAvro.newBuilder()
                        .setLinkQuality(lightDto.getLinkQuality())
                        .setLuminosity(lightDto.getLuminosity())
                        .build();

            case MOTION_SENSOR_EVENT:
                MotionSensorEventDto motionDto = (MotionSensorEventDto) dto;
                return MotionSensorAvro.newBuilder()
                        .setLinkQuality(motionDto.getLinkQuality())
                        .setMotion(motionDto.getMotion())
                        .setVoltage(motionDto.getVoltage())
                        .build();

            case SWITCH_SENSOR_EVENT:
                SwitchSensorEventDto switchDto = (SwitchSensorEventDto) dto;
                return SwitchSensorAvro.newBuilder()
                        .setState(switchDto.getState())
                        .build();

            case TEMPERATURE_SENSOR_EVENT:
                TemperatureSensorEventDto tempDto = (TemperatureSensorEventDto) dto;
                return TemperatureSensorAvro.newBuilder()
                        .setTemperatureC(tempDto.getTemperatureC())
                        .build();

            default:
                String errorMsg = String.format("Неизвестный тип события: %s", dto.getType());
                log.error(errorMsg);
                throw new IllegalArgumentException(errorMsg);
        }
    }
}

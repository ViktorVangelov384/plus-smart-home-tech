package ru.yandex.practicum.gprc.sensor.mapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.dto.sensor.ClimateSensorEventDto;
import ru.yandex.practicum.entity.SensorEventType;
import ru.yandex.practicum.grpc.telemetry.event.ClimateSensorProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;

@Slf4j
@Component
public class ClimateSensorEventMapper extends AbstractSensorEventMapper<ClimateSensorEventDto> {

    @Override
    public SensorEventType getSupportedType() {
        return SensorEventType.CLIMATE_SENSOR_EVENT;
    }

    @Override
    protected ClimateSensorEventDto createSensorEventDto() {
        return new ClimateSensorEventDto();
    }

    @Override
    protected void validateSensorSpecificData(SensorEventProto sensorEventProto) {
        if (sensorEventProto.getPayloadCase() != SensorEventProto.PayloadCase.CLIMATE_SENSOR) {
            throw new IllegalArgumentException(
                    String.format("Некорректный тип события для ClimateSensorEventMapper. " +
                                    "Ожидался CLIMATE_SENSOR, получен: %s. ID: %s",
                            sensorEventProto.getPayloadCase(),
                            sensorEventProto.getId())
            );
        }
    }

    @Override
    protected void populateSensorSpecificFields(
            ClimateSensorEventDto sensorEventDto,
            SensorEventProto sensorEventProto) {

        ClimateSensorProto climateData = sensorEventProto.getClimateSensor();

        sensorEventDto.setTemperatureC(climateData.getTemperatureC());
        sensorEventDto.setHumidity(climateData.getHumidity());
        sensorEventDto.setCo2Level(climateData.getCo2Level());
    }
}
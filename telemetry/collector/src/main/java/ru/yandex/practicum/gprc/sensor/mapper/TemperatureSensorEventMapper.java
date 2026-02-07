package ru.yandex.practicum.gprc.sensor.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.dto.sensor.TemperatureSensorEventDto;
import ru.yandex.practicum.entity.SensorEventType;
import ru.yandex.practicum.grpc.telemetry.event.TemperatureSensorProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;

@Component
public class TemperatureSensorEventMapper extends AbstractSensorEventMapper<TemperatureSensorEventDto> {

    @Override
    public SensorEventType getSupportedType() {
        return SensorEventType.TEMPERATURE_SENSOR_EVENT;
    }

    @Override
    protected TemperatureSensorEventDto createSensorEventDto() {
        return new TemperatureSensorEventDto();
    }

    @Override
    protected void validateSensorSpecificData(SensorEventProto sensorEventProto) {
        if (sensorEventProto.getPayloadCase() != SensorEventProto.PayloadCase.TEMPERATURE_SENSOR) {
            throw new IllegalArgumentException(
                    String.format("TemperatureSensorEventMapper: некорректный тип события. " +
                                    "Ожидался TEMPERATURE_SENSOR, получен %s. ID: %s",
                            sensorEventProto.getPayloadCase(),
                            sensorEventProto.getId())
            );
        }
    }

    @Override
    protected void populateSensorSpecificFields(
            TemperatureSensorEventDto sensorEventDto,
            SensorEventProto sensorEventProto) {

        TemperatureSensorProto tempData = sensorEventProto.getTemperatureSensor();

        sensorEventDto.setTemperatureC(tempData.getTemperatureC());

    }

    @Override
    protected void validateResult(TemperatureSensorEventDto sensorEventDto) {
        super.validateResult(sensorEventDto);

        float temperatureC = sensorEventDto.getTemperatureC();
        if (temperatureC < -50 || temperatureC > 100) {
            throw new IllegalStateException(
                    String.format("Температура должна быть в диапазоне -50..100°C, получено: %.1f", temperatureC)
            );
        }
    }
}

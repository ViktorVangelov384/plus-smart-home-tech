package ru.yandex.practicum.gprc.sensor.mapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.dto.sensor.SwitchSensorEventDto;
import ru.yandex.practicum.enums.SensorEventType;
import ru.yandex.practicum.grpc.telemetry.event.SwitchSensorProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;

@Slf4j
@Component
public class SwitchSensorEventMapper extends AbstractSensorEventMapper<SwitchSensorEventDto> {

    @Override
    public SensorEventType getSupportedType() {
        return SensorEventType.SWITCH_SENSOR_EVENT;
    }

    @Override
    protected SwitchSensorEventDto createSensorEventDto() {
        return new SwitchSensorEventDto();
    }

    @Override
    protected void validateSensorSpecificData(SensorEventProto sensorEventProto) {
        if (sensorEventProto.getPayloadCase() != SensorEventProto.PayloadCase.SWITCH_SENSOR) {
            throw new IllegalArgumentException(
                    String.format("SwitchSensorEventMapper: некорректный тип события. " +
                                    "Ожидался SWITCH_SENSOR, получен %s. ID: %s",
                            sensorEventProto.getPayloadCase(),
                            sensorEventProto.getId())
            );
        }
    }

    @Override
    protected void populateSensorSpecificFields(
            SwitchSensorEventDto sensorEventDto,
            SensorEventProto sensorEventProto) {

        SwitchSensorProto switchData = sensorEventProto.getSwitchSensor();

        sensorEventDto.setState(switchData.getState());
    }

    @Override
    protected void validateResult(SwitchSensorEventDto sensorEventDto) {
        super.validateResult(sensorEventDto);
    }
}
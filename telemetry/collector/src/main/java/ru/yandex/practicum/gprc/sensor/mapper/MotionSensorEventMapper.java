package ru.yandex.practicum.gprc.sensor.mapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.dto.sensor.MotionSensorEventDto;
import ru.yandex.practicum.entity.SensorEventType;
import ru.yandex.practicum.grpc.telemetry.event.MotionSensorProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;

@Slf4j
@Component
public class MotionSensorEventMapper extends AbstractSensorEventMapper<MotionSensorEventDto> {

    @Override
    public SensorEventType getSupportedType() {
        return SensorEventType.MOTION_SENSOR_EVENT;
    }

    @Override
    protected MotionSensorEventDto createSensorEventDto() {
        return new MotionSensorEventDto();
    }

    @Override
    protected void validateSensorSpecificData(SensorEventProto sensorEventProto) {
        if (sensorEventProto.getPayloadCase() != SensorEventProto.PayloadCase.MOTION_SENSOR) {
            throw new IllegalArgumentException(
                    String.format("Некорректный тип события для MotionSensorEventMapper. ID: %s",
                            sensorEventProto.getId())
            );
        }
    }

    @Override
    protected void populateSensorSpecificFields(
            MotionSensorEventDto sensorEventDto,
            SensorEventProto sensorEventProto) {

        MotionSensorProto motionData = sensorEventProto.getMotionSensor();

        sensorEventDto.setLinkQuality(motionData.getLinkQuality());
        sensorEventDto.setMotion(motionData.getMotion());
        sensorEventDto.setVoltage(motionData.getVoltage());
    }
}

package ru.yandex.practicum.gprc.sensor.mapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.dto.sensor.LightSensorEventDto;
import ru.yandex.practicum.entity.SensorEventType;
import ru.yandex.practicum.grpc.telemetry.event.LightSensorProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;

@Slf4j
@Component
public class LightSensorEventMapper extends AbstractSensorEventMapper<LightSensorEventDto> {

    @Override
    public SensorEventType getSupportedType() {
        return SensorEventType.LIGHT_SENSOR_EVENT;
    }

    @Override
    protected LightSensorEventDto createSensorEventDto() {
        return new LightSensorEventDto();
    }

    @Override
    protected void validateSensorSpecificData(SensorEventProto sensorEventProto) {
        if (sensorEventProto.getPayloadCase() != SensorEventProto.PayloadCase.LIGHT_SENSOR) {
            throw new IllegalArgumentException(
                    String.format("Некорректный тип события для LightSensorEventMapper. " +
                                    "Ожидался LIGHT_SENSOR, получен: %s. ID: %s",
                            sensorEventProto.getPayloadCase(),
                            sensorEventProto.getId())
            );
        }
    }

    @Override
    protected void populateSensorSpecificFields(
            LightSensorEventDto sensorEventDto,
            SensorEventProto sensorEventProto) {

        LightSensorProto lightData = sensorEventProto.getLightSensor();

        sensorEventDto.setLinkQuality(lightData.getLinkQuality());
        sensorEventDto.setLuminosity(lightData.getLuminosity());
    }

    @Override
    protected void validateResult(LightSensorEventDto sensorEventDto) {
        super.validateResult(sensorEventDto);

        int linkQuality = sensorEventDto.getLinkQuality();
        if (linkQuality < 0 || linkQuality > 255) {
            throw new IllegalStateException(
                    String.format("Качество связи должно быть в диапазоне 0-255, получено: %d", linkQuality)
            );
        }

        int luminosity = sensorEventDto.getLuminosity();
        if (luminosity < 0 || luminosity > 65535) {
            throw new IllegalStateException(
                    String.format("Освещенность должна быть в диапазоне 0-65535, получено: %d", luminosity)
            );
        }
    }
}





package ru.yandex.practicum.mapper.sensor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.dto.sensor.LightSensorEventDto;
import ru.yandex.practicum.dto.sensor.SensorEventDto;
import ru.yandex.practicum.enums.SensorEventType;
import ru.yandex.practicum.exception.EventProcessingException;
import ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro;
import ru.yandex.practicum.producer.EventProducer;

@Slf4j
@Component
public class LightSensorEventHandler extends BaseSensorEventHAndler<LightSensorAvro> {

    public LightSensorEventHandler(EventProducer producer) {
        super(producer);
    }

    @Override
    public SensorEventType getSupportedType() {
        return SensorEventType.LIGHT_SENSOR_EVENT;
    }

    @Override
    protected LightSensorAvro mapToAvro(SensorEventDto event) {
        try {
            LightSensorEventDto dto = (LightSensorEventDto) event;

            log.debug("Mapping light sensor: linkQuality={}, luminosity={}, lightLevel={}",
                    dto.getLinkQuality(), dto.getLuminosity(), dto.getLightLevelDescription());

            if (!dto.hasGoodConnection()) {
                log.warn("Poor connection quality: {}%", dto.getLinkQuality());
            }

            if (!dto.isSufficientLight()) {
                log.info("Insufficient light detected: {} lux", dto.getLuminosity());
            }

            return LightSensorAvro.newBuilder()
                    .setLinkQuality(validateLinkQuality(dto.getLinkQuality()))
                    .setLuminosity(validateLuminosity(dto.getLuminosity()))
                    .build();

        } catch (ClassCastException e) {
            String error = "Invalid DTO type for light sensor event";
            log.error(error, e);
            throw new EventProcessingException(error, e);
        }
    }

    private int validateLinkQuality(Integer linkQuality) {
        if (linkQuality == null) {
            log.warn("Link quality is null, using default value 0");
            return 0;
        }

        if (linkQuality < 0 || linkQuality > 100) {
            log.warn("Link quality out of range: {}% (expected 0..100)", linkQuality);
        }
        return linkQuality;
    }

    private int validateLuminosity(Integer luminosity) {
        if (luminosity == null) {
            log.warn("Luminosity is null, using default value 0");
            return 0;
        }

        if (luminosity < 0) {
            log.warn("Negative luminosity value: {}, setting to 0", luminosity);
            return 0;
        }

        if (luminosity > 100000) {
            log.warn("Luminosity exceeds max allowed value: {} lux (max 100000)", luminosity);
        }
        return luminosity;
    }
}
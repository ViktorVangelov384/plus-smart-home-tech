package ru.yandex.practicum.mapper.sensor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.dto.sensor.MotionSensorEventDto;
import ru.yandex.practicum.dto.sensor.SensorEventDto;
import ru.yandex.practicum.enums.SensorEventType;
import ru.yandex.practicum.exception.EventProcessingException;
import ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro;
import ru.yandex.practicum.producer.EventProducer;

@Slf4j
@Component
public class MotionSensorEventHandler extends BaseSensorEventHAndler<MotionSensorAvro> {

    private static final int MIN_VOLTAGE = 0;
    private static final int MAX_VOLTAGE = 5000;
    private static final int LOW_BATTERY_THRESHOLD = 3000;

    public MotionSensorEventHandler(EventProducer producer) {
        super(producer);
    }

    @Override
    protected SensorEventType getSupportedType() {
        return SensorEventType.MOTION_SENSOR_EVENT;
    }

    @Override
    protected MotionSensorAvro mapToAvro(SensorEventDto event) {
        try {
            MotionSensorEventDto dto = (MotionSensorEventDto) event;

            boolean motionDetected = dto.hasMotionDetected();

            log.debug("Mapping motion sensor: motion={}, voltage={}mV, linkQuality={}%",
                    motionDetected, dto.getVoltage(), dto.getLinkQuality());

            if (motionDetected) {
                log.info(" Motion detected!");
            }

            checkBatteryLevel(dto.getVoltage());

            return MotionSensorAvro.newBuilder()
                    .setLinkQuality(validateLinkQuality(dto.getLinkQuality()))
                    .setMotion(motionDetected)
                    .setVoltage(validateVoltage(dto.getVoltage()))
                    .build();

        } catch (ClassCastException e) {
            String error = "Invalid DTO type for motion sensor event";
            log.error(error, e);
            throw new EventProcessingException(error, e);
        }
    }

    private void checkBatteryLevel(Integer voltage) {
        if (voltage == null) return;

        if (voltage < LOW_BATTERY_THRESHOLD) {
            log.warn("Low battery: {}mV (threshold: {}mV)", voltage, LOW_BATTERY_THRESHOLD);
        }
    }

    private int validateLinkQuality(Integer linkQuality) {
        if (linkQuality == null) {
            log.warn("Link quality is null, using default 0%");
            return 0;
        }

        if (linkQuality < 0 || linkQuality > 100) {
            log.warn("Link quality out of range: {}% (expected 0..100)", linkQuality);
        }
        return linkQuality;
    }

    private int validateVoltage(Integer voltage) {
        if (voltage == null) {
            log.warn("Voltage is null, using default 0mV");
            return 0;
        }

        if (voltage < MIN_VOLTAGE) {
            log.warn("Negative voltage: {}mV, adjusting to 0", voltage);
            return 0;
        }

        if (voltage > MAX_VOLTAGE) {
            log.warn("Voltage exceeds safe limit: {}mV (max {}mV)", voltage, MAX_VOLTAGE);
        }
        return voltage;
    }
}
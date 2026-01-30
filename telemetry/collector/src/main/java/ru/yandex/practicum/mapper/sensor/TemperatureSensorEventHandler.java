package ru.yandex.practicum.mapper.sensor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.dto.sensor.SensorEventDto;
import ru.yandex.practicum.dto.sensor.TemperatureSensorEventDto;
import ru.yandex.practicum.enums.SensorEventType;
import ru.yandex.practicum.exception.EventProcessingException;
import ru.yandex.practicum.kafka.telemetry.event.TemperatureSensorAvro;
import ru.yandex.practicum.producer.EventProducer;

@Slf4j
@Component
public class TemperatureSensorEventHandler extends BaseSensorEventHAndler<TemperatureSensorAvro> {

    public TemperatureSensorEventHandler(EventProducer producer) {
        super(producer);
        log.debug("Temperature sensor handler initialized");
    }

    @Override
    public SensorEventType getSupportedType() {
        return SensorEventType.TEMPERATURE_SENSOR_EVENT;
    }

    @Override
    protected TemperatureSensorAvro mapToAvro(SensorEventDto event) {
        try {
            TemperatureSensorEventDto dto = (TemperatureSensorEventDto) event;

            log.debug("Mapping temperature sensor: {}°C ({})",
                    dto.getTemperatureC(), dto.getTemperatureDescription());

            if (!dto.isComfortableTemperature()) {
                log.info("Non-comfortable temperature detected: {}°C", dto.getTemperatureC());
            }

            return TemperatureSensorAvro.newBuilder()
                    .setId(dto.getId())
                    .setHubId(dto.getHubId())
                    .setTimestamp(dto.getTimestamp())
                    .setTemperatureC(validateTemperature(dto.getTemperatureC()))
                    .build();

        } catch (ClassCastException e) {
            String error = String.format("Invalid DTO type. Expected: TemperatureSensorEventDto, got: %s",
                    event.getClass().getSimpleName());
            log.error(error, e);
            throw new EventProcessingException(error, e);
        }
    }

    private int validateTemperature(Integer temperatureC) {
        if (temperatureC == null) {
            log.warn("Temperature is null, using default 20°C");
            return 20;
        }

        if (temperatureC < -50 || temperatureC > 50) {
            log.warn("Temperature out of range: {}°C (expected -50..50)", temperatureC);
        }
        return temperatureC;
    }
}
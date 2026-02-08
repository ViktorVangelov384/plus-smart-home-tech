package ru.yandex.practicum.mapper.sensor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.dto.sensor.ClimateSensorEventDto;
import ru.yandex.practicum.dto.sensor.SensorEventDto;
import ru.yandex.practicum.enums.SensorEventType;
import ru.yandex.practicum.exception.EventProcessingException;
import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;
import ru.yandex.practicum.producer.EventProducer;

@Slf4j
@Component
public class ClimateSensorEventHandler extends BaseSensorEventHAndler<ClimateSensorAvro> {

    public ClimateSensorEventHandler(EventProducer producer) {
        super(producer);
    }

    @Override
    public SensorEventType getSupportedType() {
        return SensorEventType.CLIMATE_SENSOR_EVENT;
    }

    @Override
    protected ClimateSensorAvro mapToAvro(SensorEventDto event) {
        try {
            ClimateSensorEventDto dto = (ClimateSensorEventDto) event;

            log.debug("Mapping climate sensor: temp={}C, humidity={}%, co2={}ppm",
                    dto.getTemperatureC(), dto.getHumidity(), dto.getCo2Level());

            return ClimateSensorAvro.newBuilder()
                    .setTemperatureC(validateTemperature(dto.getTemperatureC()))
                    .setHumidity(validateHumidity(dto.getHumidity()))
                    .setCo2Level(dto.getCo2Level())
                    .build();

        } catch (ClassCastException e) {
            String error = "Invalid DTO type for climate sensor event";
            log.error(error, e);
            throw new EventProcessingException(error, e);
        }
    }

    private int validateTemperature(int temperature) {
        if (temperature < -50 || temperature > 50) {
            log.warn("Suspicious temperature value: {}C", temperature);
        }
        return temperature;
    }

    private int validateHumidity(int humidity) {
        if (humidity < 0 || humidity > 100) {
            log.warn("Invalid humidity value: {}%", humidity);
        }
        return humidity;
    }
}
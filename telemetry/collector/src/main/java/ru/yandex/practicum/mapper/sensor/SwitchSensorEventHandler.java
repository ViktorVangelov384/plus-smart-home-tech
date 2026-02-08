package ru.yandex.practicum.mapper.sensor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.dto.sensor.SensorEventDto;
import ru.yandex.practicum.dto.sensor.SwitchSensorEventDto;
import ru.yandex.practicum.enums.SensorEventType;
import ru.yandex.practicum.exception.EventProcessingException;
import ru.yandex.practicum.kafka.telemetry.event.SwitchSensorAvro;
import ru.yandex.practicum.producer.EventProducer;

@Slf4j
@Component
public class SwitchSensorEventHandler extends BaseSensorEventHAndler<SwitchSensorAvro> {

    public SwitchSensorEventHandler(EventProducer producer) {
        super(producer);
        log.debug("Switch sensor handler initialized");
    }

    @Override
    public SensorEventType getSupportedType() {
        return SensorEventType.SWITCH_SENSOR_EVENT;
    }

    @Override
    protected SwitchSensorAvro mapToAvro(SensorEventDto event) {
        try {
            SwitchSensorEventDto dto = (SwitchSensorEventDto) event;

            Boolean state = dto.getState();

            log.debug("Mapping switch sensor: {} ({})",
                    state, dto.getStateDescription());

            if (dto.isOn()) {
                log.info("Switch turned ON");
            } else if (dto.isOff()) {
                log.info("Switch turned OFF");
            } else {
                log.warn("Switch state is indeterminate");
            }

            return SwitchSensorAvro.newBuilder()
                    .setState(validateState(state))
                    .build();

        } catch (ClassCastException e) {
            String error = String.format("Invalid DTO type. Expected: SwitchSensorEventDto, got: %s",
                    event.getClass().getSimpleName());
            log.error(error, e);
            throw new EventProcessingException(error, e);
        }
    }

    private boolean validateState(Boolean state) {
        if (state == null) {
            log.warn("Switch state is null, defaulting to OFF");
            return false;
        }
        return state;
    }
}
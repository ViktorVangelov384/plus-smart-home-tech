package ru.yandex.practicum.mapper.sensor;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.dto.sensor.SensorEventDto;
import ru.yandex.practicum.enums.SensorEventType;
import ru.yandex.practicum.exception.EventProcessingException;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.producer.EventProducer;

import static ru.yandex.practicum.config.ConfigKafka.TopicType.SENSORS_EVENTS;

@Slf4j
@Component
public abstract class BaseSensorEventHAndler<T extends SpecificRecordBase> implements SensorEventHandler {

    protected final EventProducer producer;

    protected BaseSensorEventHAndler(EventProducer producer) {
        if (producer == null) {
            throw new IllegalArgumentException("KafkaEventProducer cannot be null");
        }
        this.producer = producer;
        log.debug("Initialized {} handler", this.getClass().getSimpleName());
    }

    @Override
    public abstract SensorEventType getSupportedType();

    @Override
    public boolean canHandle(SensorEventDto event) {
        if (event == null || event.getType() == null) {
            log.warn("Cannot handle null event or event type");
            return false;
        }
        return event.getType().equals(getSupportedType());
    }

    @Override
    public void handle(SensorEventDto event) {
        try {
            validateEvent(event);

            log.debug("Processing sensor event: type={}, hubId={}, id={}",
                    event.getType(), event.getHubId(), event.getId());

            T payload = mapToAvro(event);
            SensorEventAvro eventAvro = buildSensorEventAvro(event, payload);

            producer.send(eventAvro, event.getHubId(), event.getTimestamp(), SENSORS_EVENTS);

            log.debug("Sensor event processed successfully: {}", event.getId());

        } catch (Exception e) {
            String errorMsg = String.format("Failed to process sensor event: %s", event != null ? event.getId() : "null");
            log.error(errorMsg, e);
            throw new EventProcessingException(errorMsg, e);
        }
    }

    private void validateEvent(SensorEventDto event) {
        if (event == null) {
            throw new EventProcessingException("Sensor event cannot be null");
        }
        if (event.getHubId() == null || event.getHubId().trim().isEmpty()) {
            throw new EventProcessingException("Hub ID cannot be null or empty");
        }
        if (event.getId() == null || event.getId().trim().isEmpty()) {
            throw new EventProcessingException("Event ID cannot be null or empty");
        }
    }

    private SensorEventAvro buildSensorEventAvro(SensorEventDto event, T payload) {
        return SensorEventAvro.newBuilder()
                .setHubId(event.getHubId().trim())
                .setId(event.getId().trim())
                .setTimestamp(event.getTimestamp())
                .setPayload(payload)
                .build();
    }

    protected abstract T mapToAvro(SensorEventDto event);
}
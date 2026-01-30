package ru.yandex.practicum.mapper.hub;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.dto.hub.HubEventDto;
import ru.yandex.practicum.enums.HubEventType;
import ru.yandex.practicum.exception.EventProcessingException;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.producer.EventProducer;

import static ru.yandex.practicum.config.ConfigKafka.TopicType.HUBS_EVENTS;

@Slf4j
@Component
@RequiredArgsConstructor
public abstract class BaseHubEventHandler<T extends SpecificRecordBase> implements HubEventHandler {

    protected final EventProducer producer;

    @Override
    public HubEventType getSupportedType() {
        return getSupportedType();
    }

    protected abstract T mapToAvro(HubEventDto event);

    @Override
    public boolean canHandle(HubEventDto event) {
        if (event == null || event.getType() == null) {
            log.warn("Получено событие с null-типом или null-DTO");
            return false;
        }
        return event.getType().equals(getSupportedType());
    }

    @Override
    public void handle(HubEventDto event) {
        if (event == null) {
            log.error("Попытка обработки null-события");
            throw new IllegalArgumentException("Событие не может быть null");
        }

        try {
            T payload = mapToAvro(event);

            HubEventAvro hubEvent = HubEventAvro.newBuilder()
                    .setHubId(event.getHubId())
                    .setTimestamp(event.getTimestamp())
                    .setPayload(payload)
                    .build();

            producer.send(
                    hubEvent,
                    event.getHubId(),
                    event.getTimestamp(),
                    HUBS_EVENTS
            );

            log.debug("Событие типа {} для хаба {} успешно отправлено в Kafka",
                    event.getType(), event.getHubId());

        } catch (Exception e) {
            log.error("Ошибка при обработке события типа {} для хаба {}: {}",
                    event.getType(), event.getHubId(), e.getMessage(), e);
            throw new EventProcessingException(
                    String.format("Не удалось обработать событие типа %s", event.getType()),
                    e
            );
        }
    }
}


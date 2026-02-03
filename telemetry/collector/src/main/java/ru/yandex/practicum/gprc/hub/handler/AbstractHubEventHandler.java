package ru.yandex.practicum.gprc.hub.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import ru.yandex.practicum.gprc.hub.service.HubEventHandler;
import ru.yandex.practicum.config.ConfigKafka;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.producer.EventProducer;

import java.time.Instant;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractHubEventHandler implements HubEventHandler {

    protected final EventProducer producer;

    protected abstract SpecificRecordBase mapToAvro(HubEventProto event);

    @Override
    public void handle(HubEventProto event) {
        validateEventType(event);

        log.info("Обработка события хаба {}: {}", getMessageType(), event.getHubId());

        SpecificRecordBase avroPayload = mapToAvro(event);

        Instant eventInstant = convertProtoTimestamp(event.getTimestamp());

        producer.send(avroPayload, event.getHubId(), eventInstant,
                ConfigKafka.TopicType.HUBS_EVENTS);


        log.debug("Событие отправлено в Kafka");
    }

    private void validateEventType(HubEventProto event) {
        HubEventProto.PayloadCase expectedType = getMessageType();
        HubEventProto.PayloadCase actualType = event.getPayloadCase();

        if (!actualType.equals(expectedType)) {
            String errorMessage = String.format(
                    "Неверный тип события. Ожидался: %s, Получен: %s",
                    expectedType, actualType);
            log.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private Instant convertProtoTimestamp(com.google.protobuf.Timestamp protoTimestamp) {
        return Instant.ofEpochSecond(
                protoTimestamp.getSeconds(),
                protoTimestamp.getNanos()
        );
    }
}

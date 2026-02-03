package ru.yandex.practicum.gprc.hub.handler;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioRemovedEventProto;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioRemovedEventAvro;
import ru.yandex.practicum.producer.EventProducer;

import java.time.Instant;

@Slf4j
@Component("grpcScenarioRemovedEventHandler")
public class ScenarioRemovedEventHandler extends AbstractHubEventHandler {

    public ScenarioRemovedEventHandler(EventProducer producer) {
        super(producer);
        log.debug("Инициализирован обработчик события удаления сценария");
    }

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.SCENARIO_REMOVED;
    }

    @Override
    protected SpecificRecordBase mapToAvro(HubEventProto event) {
        log.debug("Начало преобразования HubEventProto → HubEventAvro для SCENARIO_REMOVED");

        ScenarioRemovedEventProto scenarioEvent = event.getScenarioRemoved();
        validateScenarioRemovedEvent(scenarioEvent, event.getHubId());

        log.debug("Данные удаления сценария: hubId={}, имя сценария={}",
                event.getHubId(),
                scenarioEvent.getName());

        ScenarioRemovedEventAvro scenarioAvro = createScenarioRemovedAvro(scenarioEvent);

        HubEventAvro hubEvent = createHubEventAvro(event, scenarioAvro);

        log.debug("Преобразование завершено: hubId={}, удален сценарий={}",
                event.getHubId(), scenarioEvent.getName());

        return hubEvent;
    }

    private void validateScenarioRemovedEvent(ScenarioRemovedEventProto scenarioEvent, String hubId) {
        if (scenarioEvent == null) {
            log.error("Поле scenario_removed не установлено в HubEventProto для хаба {}", hubId);
            throw new IllegalArgumentException("Отсутствуют данные об удалении сценария для хаба " + hubId);
        }

        if (scenarioEvent.getName() == null || scenarioEvent.getName().isBlank()) {
            log.error("Имя сценария не указано при удалении для хаба {}", hubId);
            throw new IllegalArgumentException("Имя удаляемого сценария не может быть пустым для хаба " + hubId);
        }
    }

    private ScenarioRemovedEventAvro createScenarioRemovedAvro(ScenarioRemovedEventProto scenarioEvent) {
        return ScenarioRemovedEventAvro.newBuilder()
                .setName(scenarioEvent.getName())
                .build();
    }

    private HubEventAvro createHubEventAvro(HubEventProto event, ScenarioRemovedEventAvro scenarioAvro) {
        Instant eventInstant = Instant.ofEpochSecond(
                event.getTimestamp().getSeconds(),
                event.getTimestamp().getNanos()
        );

        return HubEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setTimestamp(eventInstant)
                .setPayload(scenarioAvro)
                .build();
    }
}

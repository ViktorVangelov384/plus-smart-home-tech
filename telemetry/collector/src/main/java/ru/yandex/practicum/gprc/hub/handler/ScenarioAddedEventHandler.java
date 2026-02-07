package ru.yandex.practicum.gprc.hub.handler;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.*;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.producer.EventProducer;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component("grpcScenarioAddedEventHandler")
public class ScenarioAddedEventHandler extends AbstractHubEventHandler {

    public ScenarioAddedEventHandler(EventProducer producer) {
        super(producer);
    }

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.SCENARIO_ADDED;
    }

    @Override
    protected SpecificRecordBase mapToAvro(HubEventProto event) {
        log.debug("Начало преобразования HubEventProto → HubEventAvro для SCENARIO_ADDED");

        ScenarioAddedEventProto scenarioEvent = event.getScenarioAdded();
        validateScenarioEvent(scenarioEvent, event.getHubId());

        log.debug("Данные сценария: hubId={}, name={}, условий={}, действий={}",
                event.getHubId(),
                scenarioEvent.getName(),
                scenarioEvent.getConditionCount(),
                scenarioEvent.getActionCount());

        List<ScenarioConditionAvro> conditions = scenarioEvent.getConditionList().stream()
                .map(this::mapConditionToAvro)
                .collect(Collectors.toList());

        List<DeviceActionAvro> actions = scenarioEvent.getActionList().stream()
                .map(this::mapActionToAvro)
                .collect(Collectors.toList());

        ScenarioAddedEventAvro scenarioAvro = ScenarioAddedEventAvro.newBuilder()
                .setName(scenarioEvent.getName())
                .setConditions(conditions)
                .setActions(actions)
                .build();

        Instant eventInstant = Instant.ofEpochSecond(
                event.getTimestamp().getSeconds(),
                event.getTimestamp().getNanos()
        );

        HubEventAvro hubEventAvro = HubEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setTimestamp(eventInstant)
                .setPayload(scenarioAvro)
                .build();

        log.debug("Преобразование завершено: hubId={}, имя сценария={}",
                event.getHubId(), scenarioEvent.getName());

        return hubEventAvro;
    }

    private void validateScenarioEvent(ScenarioAddedEventProto scenarioEvent, String hubId) {
        if (scenarioEvent == null) {
            log.error("Поле scenario_added не установлено в HubEventProto для хаба {}", hubId);
            throw new IllegalArgumentException("Отсутствуют данные о добавлении сценария для хаба " + hubId);
        }
    }

    private ScenarioConditionAvro mapConditionToAvro(ScenarioConditionProto condition) {
        int conditionValue = extractConditionValue(condition);

        return ScenarioConditionAvro.newBuilder()
                .setSensorId(condition.getSensorId())
                .setType(ConditionTypeAvro.valueOf(condition.getType().name()))
                .setOperation(ConditionOperationAvro.valueOf(condition.getOperation().name()))
                .setValue(conditionValue)
                .build();
    }

    private int extractConditionValue(ScenarioConditionProto condition) {
        if (condition.getValueCase() == ScenarioConditionProto.ValueCase.BOOL_VALUE) {
            return condition.getBoolValue() ? 1 : 0;
        }
        return condition.getIntValue();
    }

    private DeviceActionAvro mapActionToAvro(DeviceActionProto action) {
        return DeviceActionAvro.newBuilder()
                .setSensorId(action.getSensorId())
                .setType(ActionTypeAvro.valueOf(action.getType().name()))
                .setValue(action.getValue())
                .build();
    }
}

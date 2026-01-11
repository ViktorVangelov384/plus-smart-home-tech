package ru.yandex.practicum.mapper.hub;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.dto.hub.HubEventDto;
import ru.yandex.practicum.dto.scenario.ScenarioAddedEventDto;
import ru.yandex.practicum.dto.scenario.ScenarioConditionDto;
import ru.yandex.practicum.dto.device.DeviceActionDto;
import ru.yandex.practicum.enums.ActionType;
import ru.yandex.practicum.enums.HubEventType;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.producer.EventProducer;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ScenarioAddedEventHandler extends BaseHubEventHandler<ScenarioAddedEventAvro> {

    public ScenarioAddedEventHandler(EventProducer producer) {
        super(producer);
        log.info("ScenarioAddedEventHandler создан с пакетом: ru.yandex.practicum.mapper");
    }

    @Override
    protected HubEventType getSupportedType() {
        return HubEventType.SCENARIO_ADDED;
    }

    @Override
    protected ScenarioAddedEventAvro mapToAvro(HubEventDto event) {
        ScenarioAddedEventDto dto = (ScenarioAddedEventDto) event;

        List<ScenarioConditionAvro> conditions = dto.getConditions().stream()
                .map(this::mapCondition)
                .collect(Collectors.toList());

        List<DeviceActionAvro> actions = dto.getActions().stream()
                .map(this::mapAction)
                .collect(Collectors.toList());

        return ScenarioAddedEventAvro.newBuilder()
                .setName(dto.getName())
                .setConditions(conditions)
                .setActions(actions)
                .build();
    }

    private ScenarioConditionAvro mapCondition(ScenarioConditionDto condition) {
        ConditionTypeAvro conditionType = ConditionTypeAvro.valueOf(condition.getType().name());
        ConditionOperationAvro operation = ConditionOperationAvro.valueOf(condition.getOperation().name());

        return ScenarioConditionAvro.newBuilder()
                .setSensorId(condition.getSensorId())
                .setType(conditionType)
                .setOperation(operation)
                .setValue(condition.getValue())
                .build();
    }

    private DeviceActionAvro mapAction(DeviceActionDto action) {
        return DeviceActionAvro.newBuilder()
                .setSensorId(action.getSensorId())
                .setType(mapActionType(action.getType()))
                .setValue(action.getValue())
                .build();
    }

    private ActionTypeAvro mapActionType(ActionType type) {
        return ActionTypeAvro.valueOf(type.name());
    }
}
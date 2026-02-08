package ru.yandex.practicum.consumer.hub.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.model.Action;
import ru.yandex.practicum.model.Condition;
import ru.yandex.practicum.model.Scenario;
import ru.yandex.practicum.enums.ActionType;
import ru.yandex.practicum.enums.ConditionType;
import ru.yandex.practicum.enums.ConditionTypeOperation;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.consumer.hub.service.SensorService;

@Component
@RequiredArgsConstructor
public class AvroToEntityConverter {

    private final SensorService sensorService;

    public Scenario convertScenarioEvent(ScenarioAddedEventAvro scenarioEvent, String hubId) {
        Scenario scenarioEntity = new Scenario();
        scenarioEntity.setHubId(hubId);
        scenarioEntity.setName(scenarioEvent.getName());

        processScenarioConditions(scenarioEvent, hubId, scenarioEntity);
        processScenarioActions(scenarioEvent, hubId, scenarioEntity);

        return scenarioEntity;
    }

    private void processScenarioConditions(ScenarioAddedEventAvro event,
                                           String hubId,
                                           Scenario scenario) {
        for (ScenarioConditionAvro conditionAvro : event.getConditions()) {
            String sensorId = conditionAvro.getSensorId();

            ensureDeviceRegistered(sensorId, hubId);
            Condition conditionEntity = convertConditionEvent(conditionAvro);

            scenario.getSensorConditions().put(sensorId, conditionEntity);
        }
    }

    private void processScenarioActions(ScenarioAddedEventAvro event,
                                        String hubId,
                                        Scenario scenario) {
        for (DeviceActionAvro actionAvro : event.getActions()) {
            String sensorId = actionAvro.getSensorId();

            ensureDeviceRegistered(sensorId, hubId);
            Action actionEntity = convertActionEvent(actionAvro);

            scenario.getSensorActions().put(sensorId, actionEntity);
        }
    }

    public Condition convertConditionEvent(ScenarioConditionAvro conditionAvro) {
        Condition conditionEntity = new Condition();

        mapConditionType(conditionAvro, conditionEntity);
        mapConditionOperation(conditionAvro, conditionEntity);
        extractConditionValue(conditionAvro, conditionEntity);

        return conditionEntity;
    }

    public Action convertActionEvent(DeviceActionAvro actionAvro) {
        Action actionEntity = new Action();

        mapActionType(actionAvro, actionEntity);
        extractActionValue(actionAvro, actionEntity);

        return actionEntity;
    }

    private void ensureDeviceRegistered(String deviceId, String hubId) {
        sensorService.registerOrUpdateDevice(deviceId, hubId);
    }

    private void mapConditionType(ScenarioConditionAvro source, Condition target) {
        String typeName = source.getType().name();
        ConditionType conditionType = ConditionType.valueOf(typeName);
        target.setType(conditionType);
    }

    private void mapConditionOperation(ScenarioConditionAvro source, Condition target) {
        String operationName = source.getOperation().name();
        ConditionTypeOperation operation = ConditionTypeOperation.valueOf(operationName);
        target.setOperation(operation);
    }

    private void extractConditionValue(ScenarioConditionAvro source, Condition target) {
        Object unionValue = source.getValue();

        if (unionValue instanceof Integer integerValue) {
            target.setValue(integerValue);
        } else if (unionValue instanceof Boolean booleanValue) {
            target.setValue(booleanValue ? 1 : 0);
        }
    }

    private void mapActionType(DeviceActionAvro source, Action target) {
        String typeName = source.getType().name();
        ActionType actionType = ActionType.valueOf(typeName);
        target.setType(actionType);
    }

    private void extractActionValue(DeviceActionAvro source, Action target) {
        Object unionValue = source.getValue();

        if (unionValue instanceof Integer integerValue) {
            target.setValue(integerValue);
        }
    }
}

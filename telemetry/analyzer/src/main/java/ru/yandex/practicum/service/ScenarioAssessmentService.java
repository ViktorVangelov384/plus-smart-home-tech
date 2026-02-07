package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.entity.Action;
import ru.yandex.practicum.entity.Condition;
import ru.yandex.practicum.entity.Scenario;
import ru.yandex.practicum.enums.ConditionType;
import ru.yandex.practicum.enums.ConditionTypeOperation;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.mapper.DeviceActionGrpcConverter;
import ru.yandex.practicum.repository.ScenarioDao;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScenarioAssessmentService {

    private final ScenarioDao scenarioDao;
    private final HubRouterGrpcClient hubRouterClient;
    private final DeviceActionGrpcConverter actionConverter;

    @Transactional(readOnly = true)
    public void assessSensorData(SensorsSnapshotAvro sensorSnapshot) {
        String targetHub = sensorSnapshot.getHubId();
        Map<String, SensorStateAvro> currentReadings = sensorSnapshot.getSensorsState();

        log.info("Начинаю оценку состояния для хаба '{}'. Зарегистрировано датчиков: {}",
                targetHub, currentReadings.size());

        List<Scenario> applicableScenarios = scenarioDao.findByHubId(targetHub);
        if (applicableScenarios.isEmpty()) {
            log.debug("Для хаба '{}' не найдено активных сценариев", targetHub);
            return;
        }

        log.debug("Обнаружено {} сценариев для оценки", applicableScenarios.size());

        for (Scenario currentScenario : applicableScenarios) {
            processSingleScenario(currentScenario, currentReadings);
        }
    }

    private void processSingleScenario(Scenario targetScenario, Map<String, SensorStateAvro> sensorReadings) {
        String scenarioIdentifier = targetScenario.getName();
        boolean scenarioQualifies = true;

        log.debug("Проверяю соответствие сценария '{}'", scenarioIdentifier);

        for (Map.Entry<String, Condition> conditionEntry : targetScenario.getSensorConditions().entrySet()) {
            String deviceIdentifier = conditionEntry.getKey();
            Condition validationCondition = conditionEntry.getValue();

            SensorStateAvro deviceState = sensorReadings.get(deviceIdentifier);
            if (deviceState == null) {
                log.warn("Показания устройства '{}' отсутствуют в текущем снимке", deviceIdentifier);
                scenarioQualifies = false;
                break;
            }

            if (!validateDeviceCondition(validationCondition, deviceState)) {
                scenarioQualifies = false;
                break;
            }
        }

        if (scenarioQualifies) {
            log.info("Сценарий '{}' соответствует условиям. Инициирую выполнение действий.",
                    scenarioIdentifier);
            triggerScenarioActions(targetScenario);
        } else {
            log.debug("Сценарий '{}' не соответствует условиям", scenarioIdentifier);
        }
    }

    private boolean validateDeviceCondition(Condition validationRule, SensorStateAvro deviceState) {
        Integer measuredValue = retrieveSensorMeasurement(validationRule.getType(), deviceState);
        Integer thresholdValue = validationRule.getValue();

        if (measuredValue == null || thresholdValue == null) {
            log.warn("Невозможно выполнить проверку. Измеренное: {}, Требуемое: {}",
                    measuredValue, thresholdValue);
            return false;
        }

        boolean evaluationResult = evaluateCondition(
                validationRule.getOperation(),
                measuredValue,
                thresholdValue
        );

        log.trace("Результат проверки условия. Тип: {}, Операция: {}, "
                        + "Фактическое: {}, Ожидаемое: {}, Результат: {}",
                validationRule.getType(), validationRule.getOperation(),
                measuredValue, thresholdValue, evaluationResult);

        return evaluationResult;
    }

    private boolean evaluateCondition(ConditionTypeOperation operation,
                                      Integer actualMeasurement,
                                      Integer referenceValue) {
        return switch (operation) {
            case EQUALS -> actualMeasurement.equals(referenceValue);
            case GREATER_THAN -> actualMeasurement > referenceValue;
            case LOWER_THAN -> actualMeasurement < referenceValue;
        };
    }

    private Integer retrieveSensorMeasurement(ConditionType measurementType,
                                              SensorStateAvro deviceState) {
        Object rawSensorData = deviceState.getData();

        try {
            return switch (measurementType) {
                case MOTION -> {
                    if (rawSensorData instanceof MotionSensorAvro motionData) {
                        yield motionData.getMotion() ? 1 : 0;
                    }
                    yield null;
                }
                case TEMPERATURE -> {
                    if (rawSensorData instanceof TemperatureSensorAvro tempData) {
                        yield tempData.getTemperatureC();
                    }
                    if (rawSensorData instanceof ClimateSensorAvro climateData) {
                        yield climateData.getTemperatureC();
                    }
                    yield null;
                }
                case LUMINOSITY -> {
                    if (rawSensorData instanceof LightSensorAvro lightData) {
                        yield lightData.getLuminosity();
                    }
                    yield null;
                }
                case SWITCH -> {
                    if (rawSensorData instanceof SwitchSensorAvro switchData) {
                        yield switchData.getState() ? 1 : 0;
                    }
                    yield null;
                }
                case HUMIDITY -> {
                    if (rawSensorData instanceof ClimateSensorAvro climateData) {
                        yield climateData.getHumidity();
                    }
                    yield null;
                }
                case CO2LEVEL -> {
                    if (rawSensorData instanceof ClimateSensorAvro climateData) {
                        yield climateData.getCo2Level();
                    }
                    yield null;
                }
            };
        } catch (Exception extractionError) {
            log.error("Сбой при извлечении данных. Тип: {}, Данные: {}",
                    measurementType, rawSensorData.getClass().getSimpleName(), extractionError);
            return null;
        }
    }

    private void triggerScenarioActions(Scenario targetScenario) {
        String scenarioName = targetScenario.getName();
        log.debug("Активация действий сценария '{}'", scenarioName);

        for (Map.Entry<String, Action> actionEntry : targetScenario.getSensorActions().entrySet()) {
            String targetDevice = actionEntry.getKey();
            Action deviceAction = actionEntry.getValue();

            try {
                var deviceCommand = actionConverter.convertToGrpcRequest(
                        targetScenario,
                        targetScenario.getHubId(),
                        targetDevice,
                        deviceAction
                );

                hubRouterClient.transmitDeviceCommand(deviceCommand);
                log.info("Команда передана. Сценарий: '{}', Устройство: '{}'",
                        scenarioName, targetDevice);

            } catch (Exception commandError) {
                log.error("Не удалось выполнить действие. Сценарий: '{}', Устройство: '{}'",
                        scenarioName, targetDevice, commandError);
            }
        }

        log.info("Все действия сценария '{}' обработаны", scenarioName);
    }
}

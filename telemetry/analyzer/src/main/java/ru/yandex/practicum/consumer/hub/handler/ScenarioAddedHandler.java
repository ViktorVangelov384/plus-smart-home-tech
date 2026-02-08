package ru.yandex.practicum.consumer.hub.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.consumer.hub.mapper.AvroToEntityConverter;
import ru.yandex.practicum.model.Scenario;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.repository.ScenarioDao;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScenarioAddedHandler implements EventHandler {

    private final ScenarioDao scenarioDao;
    private final AvroToEntityConverter avroToEntityConverter;

    @Override
    public boolean canHandle(Object eventData) {
        return eventData instanceof ScenarioAddedEventAvro;
    }

    @Override
    @Transactional
    public void handle(String targetHubId, Object eventData) {
        ScenarioAddedEventAvro scenarioEvent = (ScenarioAddedEventAvro) eventData;
        String scenarioIdentifier = scenarioEvent.getName();

        log.debug("Получено событие добавления сценария. Хаб: {}, Имя: {}",
                targetHubId, scenarioIdentifier);

        removeExistingScenarioIfPresent(targetHubId, scenarioIdentifier);

        Scenario newScenario = convertEventToEntity(scenarioEvent, targetHubId);
        persistScenario(newScenario);

        logRegistrationComplete(scenarioIdentifier, targetHubId, scenarioEvent);
    }

    private void removeExistingScenarioIfPresent(String hubId, String scenarioName) {
        Optional<Scenario> existingScenario = scenarioDao.findByHubIdAndName(hubId, scenarioName);

        existingScenario.ifPresent(scenario -> {
            scenarioDao.delete(scenario);
            log.debug("Удален существующий сценарий: {}", scenarioName);
        });
    }

    private Scenario convertEventToEntity(ScenarioAddedEventAvro event, String hubId) {
        return avroToEntityConverter.convertScenarioEvent(event, hubId);
    }

    private void persistScenario(Scenario scenario) {
        scenarioDao.save(scenario);
    }

    private void logRegistrationComplete(String scenarioName, String hubId, ScenarioAddedEventAvro event) {
        int conditionsCount = event.getConditions().size();
        int actionsCount = event.getActions().size();

        log.info("Сценарий успешно зарегистрирован. Имя: '{}', Хаб: '{}', "
                        + "Условий: {}, Действий: {}",
                scenarioName, hubId, conditionsCount, actionsCount);
    }
}

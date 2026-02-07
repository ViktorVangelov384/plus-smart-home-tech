package ru.yandex.practicum.consumer.hub.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.entity.Scenario;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioRemovedEventAvro;
import ru.yandex.practicum.repository.ScenarioDao;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScenarioRemovedHandler implements EventHandler {

    private final ScenarioDao scenarioDao;

    @Override
    public boolean canHandle(Object eventData) {
        return eventData instanceof ScenarioRemovedEventAvro;
    }

    @Override
    public void handle(String sourceHubId, Object eventData) {
        ScenarioRemovedEventAvro deletionEvent = (ScenarioRemovedEventAvro) eventData;
        String scenarioIdentifier = deletionEvent.getName();
        executeScenarioRemoval(sourceHubId, scenarioIdentifier);
    }

    private void executeScenarioRemoval(String hubId, String scenarioName) {
        Optional<Scenario> targetScenario = scenarioDao.findByHubIdAndName(hubId, scenarioName);

        if (targetScenario.isPresent()) {
            removeScenarioFromRegistry(targetScenario.get(), scenarioName, hubId);
        } else {
            logScenarioNotFound(scenarioName, hubId);
        }
    }

    private void removeScenarioFromRegistry(Scenario scenarioToRemove,
                                            String scenarioName,
                                            String hubId) {
        scenarioDao.delete(scenarioToRemove);
    }

    private void logScenarioNotFound(String scenarioName, String hubId) {
        log.warn("Не удалось найти сценарий для удаления. Имя: '{}', Хаб: '{}'",
                scenarioName, hubId);
    }
}

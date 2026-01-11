package ru.yandex.practicum.mapper.hub;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.dto.hub.HubEventDto;
import ru.yandex.practicum.dto.scenario.ScenarioRemoveEventDto;
import ru.yandex.practicum.enums.HubEventType;
import ru.yandex.practicum.exception.EventProcessingException;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioRemovedEventAvro;
import ru.yandex.practicum.producer.EventProducer;

@Slf4j
@Component
public class ScenarioRemovedEventHandler extends BaseHubEventHandler<ScenarioRemovedEventAvro> {

    public ScenarioRemovedEventHandler(EventProducer producer) {
        super(producer);
    }

    @Override
    protected HubEventType getSupportedType() {
        return HubEventType.SCENARIO_REMOVED;
    }

    @Override
    protected ScenarioRemovedEventAvro mapToAvro(HubEventDto event) {
        try {
            validateEvent(event);
            ScenarioRemoveEventDto dto = (ScenarioRemoveEventDto) event;

            return ScenarioRemovedEventAvro.newBuilder()
                    .setName(dto.getName().trim())
                    .build();

        } catch (ClassCastException e) {
            String error = String.format("Invalid DTO type. Expected: ScenarioRemovedEventDto, got: %s",
                    event.getClass().getSimpleName());
            log.error(error);
            throw new EventProcessingException(error, e);
        }
    }

    private void validateEvent(HubEventDto event) {
        if (event.getHubId() == null || event.getHubId().trim().isEmpty()) {
            throw new EventProcessingException("Hub ID cannot be null or empty");
        }

        ScenarioRemoveEventDto dto = (ScenarioRemoveEventDto) event;
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new EventProcessingException("Scenario name cannot be null or empty");
        }
    }
}

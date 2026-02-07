package ru.yandex.practicum.mapper.hub;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.dto.device.DeviceRemovedEventDto;
import ru.yandex.practicum.dto.hub.HubEventDto;
import ru.yandex.practicum.entity.HubEventType;
import ru.yandex.practicum.exception.EventProcessingException;
import ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro;
import ru.yandex.practicum.producer.EventProducer;

@Slf4j
@Component
public class DeviceRemovedEventHandler extends BaseHubEventHandler<DeviceRemovedEventAvro> {

    public DeviceRemovedEventHandler(EventProducer producer) {
        super(producer);
    }

    @Override
    public HubEventType getSupportedType() {
        return HubEventType.DEVICE_REMOVED;
    }

    @Override
    protected DeviceRemovedEventAvro mapToAvro(HubEventDto event) {
        try {
            validateHubId(event);
            DeviceRemovedEventDto dto = (DeviceRemovedEventDto) event;

            return DeviceRemovedEventAvro.newBuilder()
                    .setId(sanitizeId(dto.getId()))
                    .build();

        } catch (ClassCastException e) {
            String error = String.format("Invalid DTO type. Expected: DeviceRemovedEventDto, got: %s",
                    event.getClass().getSimpleName());
            log.error(error);
            throw new EventProcessingException(error, e);
        }
    }

    private void validateHubId(HubEventDto event) {
        if (event.getHubId() == null || event.getHubId().trim().isEmpty()) {
            throw new EventProcessingException("Hub ID cannot be null or empty");
        }
    }

    private String sanitizeId(String id) {
        return id.trim();
    }
}
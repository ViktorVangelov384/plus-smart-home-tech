package ru.yandex.practicum.mapper.hub;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.dto.device.DeviceAddedEventDto;
import ru.yandex.practicum.dto.hub.HubEventDto;
import ru.yandex.practicum.enums.HubEventType;

import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceTypeAvro;
import ru.yandex.practicum.producer.EventProducer;

@Slf4j
@Component
public class DeviceAddedEventHandler extends BaseHubEventHandler<DeviceAddedEventAvro> {

    public DeviceAddedEventHandler(EventProducer producer) {
        super(producer);
    }

    @Override
    public HubEventType getSupportedType() {
        return HubEventType.DEVICE_ADDED;
    }

    @Override
    protected DeviceAddedEventAvro mapToAvro(HubEventDto event) {
        if (!canHandle(event)) {
            log.error("Cannot handle event type: {}", event.getType());
            throw new IllegalArgumentException("Unsupported event type: " + event.getType());
        }

        DeviceAddedEventDto deviceEvent = (DeviceAddedEventDto) event;

        validateRequiredFields(deviceEvent);

        DeviceTypeAvro deviceTypeAvro = DeviceTypeAvro.valueOf(deviceEvent.getDeviceType().name());

        return DeviceAddedEventAvro.newBuilder()
                .setId(deviceEvent.getId())
                .setType(deviceTypeAvro)
                .build();
    }

    private void validateRequiredFields(DeviceAddedEventDto event) {
        if (event.getId() == null || event.getId().isBlank()) {
            throw new IllegalArgumentException("Device ID is required");
        }
        if (event.getDeviceType() == null) {
            throw new IllegalArgumentException("Device type is required");
        }
    }
}
package ru.yandex.practicum.consumer.hub.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.entity.Sensor;
import ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro;
import ru.yandex.practicum.repository.SensorDao;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceRemovalHandler implements EventHandler {

    private final SensorDao sensorDao;

    @Override
    public boolean canHandle(Object eventData) {
        return eventData instanceof DeviceRemovedEventAvro;
    }

    @Override
    public void handle(String sourceHubId, Object eventData) {
        DeviceRemovedEventAvro removalEvent = (DeviceRemovedEventAvro) eventData;
        String deviceIdentifier = removalEvent.getId();
        processDeviceRemoval(deviceIdentifier, sourceHubId);
    }

    private void processDeviceRemoval(String deviceId, String hubId) {
        Optional<Sensor> targetDevice = sensorDao.findByIdAndHubId(deviceId, hubId);

        if (targetDevice.isPresent()) {
            removeDeviceFromRegistry(targetDevice.get(), deviceId, hubId);
        } else {
            logDeviceNotFound(deviceId, hubId);
        }
    }

    private void removeDeviceFromRegistry(Sensor deviceToRemove, String deviceId, String hubId) {
        sensorDao.delete(deviceToRemove);
    }

    private void logDeviceNotFound(String deviceId, String hubId) {
        log.warn("Не удалось найти устройство для удаления. Идентификатор: {}, Хаб: {}",
                deviceId, hubId);
    }
}

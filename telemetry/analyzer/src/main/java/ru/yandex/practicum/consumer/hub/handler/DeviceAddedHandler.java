package ru.yandex.practicum.consumer.hub.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.model.Sensor;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.repository.SensorDao;

import java.util.Optional;


@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceAddedHandler implements EventHandler {

    private final SensorDao sensorDao;

    @Override
    public boolean canHandle(Object eventData) {
        return eventData instanceof DeviceAddedEventAvro;
    }

    @Override
    public void handle(String targetHubId, Object eventData) {
        DeviceAddedEventAvro additionEvent = (DeviceAddedEventAvro) eventData;
        String newDeviceId = additionEvent.getId();

        Optional<Sensor> existingDevice = sensorDao.findById(newDeviceId);

        existingDevice.ifPresentOrElse(
                device -> {
                    if (!targetHubId.equals(device.getHubId())) {
                        log.warn("Устройство {} зарегистрировано в другом хабе: текущий={}, полученный={}",
                                newDeviceId, device.getHubId(), targetHubId);
                    } else {
                        log.debug("Устройство {} уже существует в хабе {}",
                                newDeviceId, targetHubId);
                    }
                },
                () -> {
                    Sensor newDevice = new Sensor();
                    newDevice.setId(newDeviceId);
                    newDevice.setHubId(targetHubId);

                    sensorDao.save(newDevice);
                }
        );
    }
}


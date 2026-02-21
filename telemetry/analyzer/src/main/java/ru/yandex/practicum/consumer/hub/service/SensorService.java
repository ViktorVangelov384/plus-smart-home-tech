package ru.yandex.practicum.consumer.hub.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.model.Sensor;
import ru.yandex.practicum.repository.SensorDao;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SensorService {
    private final SensorDao sensorDao;

    public void registerOrUpdateDevice(String deviceIdentifier, String targetHubId) {
        Optional<Sensor> existingDevice = sensorDao.findByIdAndHubId(deviceIdentifier, targetHubId);

        if (existingDevice.isPresent()) {
            log.debug("Устройство {} уже зарегистрировано в хабе {}",
                    deviceIdentifier, targetHubId);
        } else {
            Optional<Sensor> deviceInOtherHub = findDeviceInAnyHub(deviceIdentifier);
            if (deviceInOtherHub.isPresent()) {
                log.warn("Устройство {} принадлежит другому хабу: {} (ожидался: {})",
                        deviceIdentifier, deviceInOtherHub.get().getHubId(), targetHubId);
            } else {
                createNewDevice(deviceIdentifier, targetHubId);
            }
        }
    }

    private Optional<Sensor> findDeviceInAnyHub(String deviceIdentifier) {
        return Optional.empty();
    }

    private void createNewDevice(String deviceIdentifier, String hubId) {
        Sensor newDevice = new Sensor();
        newDevice.setId(deviceIdentifier);
        newDevice.setHubId(hubId);

        sensorDao.save(newDevice);
        log.info("Зарегистрировано новое устройство. Идентификатор: {}, Хаб: {}",
                deviceIdentifier, hubId);
    }
}
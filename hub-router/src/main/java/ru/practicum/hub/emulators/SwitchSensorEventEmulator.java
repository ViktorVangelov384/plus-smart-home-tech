package ru.practicum.hub.emulators;

import lombok.RequiredArgsConstructor;
import ru.practicum.hub.config.SwitchSensorConfig;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.grpc.telemetry.event.SwitchSensorProto;

@RequiredArgsConstructor
public class SwitchSensorEventEmulator extends AbstractSensorEmulator {

    private final SwitchSensorConfig sensorConfig;

    @Override
    public SensorEventProto emulateEvent() {
        boolean isOn = generateRandomBoolean();

        return SensorEventProto.newBuilder()
                .setId(sensorConfig.getId())
                .setTimestamp(currentTimestamp())
                .setSwitchSensor(
                        SwitchSensorProto.newBuilder()
                                .setState(isOn)
                                .build()
                )
                .build();
    }
}
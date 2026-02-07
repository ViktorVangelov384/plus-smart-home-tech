package ru.practicum.hub.emulators;

import lombok.RequiredArgsConstructor;
import ru.practicum.hub.config.MotionSensorConfig;
import ru.yandex.practicum.grpc.telemetry.event.MotionSensorProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;

@RequiredArgsConstructor
public class MotionSensorEventEmulator extends AbstractSensorEmulator {
    private final MotionSensorConfig sensorConfig;

    @Override
    public SensorEventProto emulateEvent() {
        MotionSensorProto motionData = MotionSensorProto.newBuilder()
                .setLinkQuality(generateInRange(
                        sensorConfig.getLinkQuality().getMinValue(),
                        sensorConfig.getLinkQuality().getMaxValue()))
                .setMotion(generateRandomBoolean())
                .setVoltage(generateInRange(
                        sensorConfig.getVoltage().getMinValue(),
                        sensorConfig.getVoltage().getMaxValue()))
                .build();

        return SensorEventProto.newBuilder()
                .setId(sensorConfig.getId())
                .setTimestamp(currentTimestamp())
                .setMotionSensor(motionData)
                .build();
    }
}

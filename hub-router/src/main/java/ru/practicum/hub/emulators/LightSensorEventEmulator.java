package ru.practicum.hub.emulators;

import lombok.RequiredArgsConstructor;
import ru.practicum.hub.config.LightSensorConfig;
import ru.yandex.practicum.grpc.telemetry.event.LightSensorProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;

@RequiredArgsConstructor
public class LightSensorEventEmulator extends AbstractSensorEmulator {
    private final LightSensorConfig sensorConfig;

    @Override
    public SensorEventProto emulateEvent() {
        int luminosity = generateInRange(
                sensorConfig.getLuminosity().getMinValue(),
                sensorConfig.getLuminosity().getMaxValue()
        );
        int linkQuality = generateInRange(
                sensorConfig.getLinkQuality().getMinValue(),
                sensorConfig.getLinkQuality().getMaxValue()
        );

        return SensorEventProto.newBuilder()
                .setId(sensorConfig.getId())
                .setTimestamp(currentTimestamp())
                .setLightSensor(
                        LightSensorProto.newBuilder()
                                .setLuminosity(luminosity)
                                .setLinkQuality(linkQuality)
                                .build()
                )
                .build();
    }
}

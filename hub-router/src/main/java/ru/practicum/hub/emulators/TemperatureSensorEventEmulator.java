package ru.practicum.hub.emulators;

import lombok.RequiredArgsConstructor;
import ru.practicum.hub.config.TemperatureSensorConfig;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.grpc.telemetry.event.TemperatureSensorProto;

@RequiredArgsConstructor
public class TemperatureSensorEventEmulator extends AbstractSensorEmulator {

    private final TemperatureSensorConfig sensorConfig;

    @Override
    public SensorEventProto emulateEvent() {
        int temperatureCelsius = generateInRange(
                sensorConfig.getTemperature().getMinValue(),
                sensorConfig.getTemperature().getMaxValue()
        );

        int temperatureFahrenheit = calculateFahrenheit(temperatureCelsius);

        TemperatureSensorProto tempData = TemperatureSensorProto.newBuilder()
                .setTemperatureC(temperatureCelsius)
                .setTemperatureF(temperatureFahrenheit)
                .build();

        return SensorEventProto.newBuilder()
                .setId(sensorConfig.getId())
                .setTimestamp(currentTimestamp())
                .setTemperatureSensor(tempData)
                .build();
    }

    private int calculateFahrenheit(int celsius) {
        return (int) (celsius * 1.8 + 32);
    }
}

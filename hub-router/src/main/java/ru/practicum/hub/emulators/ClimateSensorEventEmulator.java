package ru.practicum.hub.emulators;

import lombok.RequiredArgsConstructor;
import ru.practicum.hub.config.ClimateSensorConfig;
import ru.yandex.practicum.grpc.telemetry.event.ClimateSensorProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;

@RequiredArgsConstructor
public class ClimateSensorEventEmulator extends AbstractSensorEmulator {

    private final ClimateSensorConfig sensorConfig;

    @Override
    public SensorEventProto emulateEvent() {
        int temperature = generateInRange(
                sensorConfig.getTemperature().getMinValue(),
                sensorConfig.getTemperature().getMaxValue()
        );

        int humidity = generateInRange(
                sensorConfig.getHumidity().getMinValue(),
                sensorConfig.getHumidity().getMaxValue()
        );

        int co2Level = generateInRange(
                sensorConfig.getCo2Level().getMinValue(),
                sensorConfig.getCo2Level().getMaxValue()
        );

        ClimateSensorProto climateData = ClimateSensorProto.newBuilder()
                .setTemperatureC(temperature)
                .setHumidity(humidity)
                .setCo2Level(co2Level)
                .build();

        return SensorEventProto.newBuilder()
                .setId(sensorConfig.getId())
                .setTimestamp(currentTimestamp())
                .setClimateSensor(climateData)
                .build();
    }
}

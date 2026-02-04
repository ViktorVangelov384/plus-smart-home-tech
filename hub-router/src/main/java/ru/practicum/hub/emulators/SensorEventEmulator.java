package ru.practicum.hub.emulators;

import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;

public interface SensorEventEmulator {

    SensorEventProto emulateEvent();
}

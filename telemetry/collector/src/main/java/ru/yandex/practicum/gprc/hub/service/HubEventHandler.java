package ru.yandex.practicum.gprc.hub.service;

import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;

public interface HubEventHandler {
    HubEventProto.PayloadCase getMessageType();
    void handle(HubEventProto event);
}

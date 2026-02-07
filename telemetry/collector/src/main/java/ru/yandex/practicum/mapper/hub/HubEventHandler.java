package ru.yandex.practicum.mapper.hub;

import ru.yandex.practicum.dto.hub.HubEventDto;
import ru.yandex.practicum.entity.HubEventType;

public interface HubEventHandler {

    boolean canHandle(HubEventDto event);

    void handle(HubEventDto event);

    HubEventType getSupportedType();
}

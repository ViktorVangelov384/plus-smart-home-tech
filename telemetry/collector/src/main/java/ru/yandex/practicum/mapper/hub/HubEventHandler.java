package ru.yandex.practicum.mapper.hub;

import ru.yandex.practicum.dto.hub.HubEventDto;

public interface HubEventHandler {

    boolean canHandle(HubEventDto event);

    void handle(HubEventDto event);
}

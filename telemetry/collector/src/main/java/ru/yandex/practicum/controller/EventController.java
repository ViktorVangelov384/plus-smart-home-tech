package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.hub.HubEventDto;
import ru.yandex.practicum.dto.sensor.SensorEventDto;
import ru.yandex.practicum.entity.HubEventType;
import ru.yandex.practicum.entity.SensorEventType;
import ru.yandex.practicum.mapper.hub.HubEventHandler;
import ru.yandex.practicum.mapper.sensor.SensorEventHandler;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final Map<SensorEventType, SensorEventHandler> sensorEventHandlers;
    private final Map<HubEventType, HubEventHandler> hubEventHandlers;

    @PostMapping("/sensors")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void collectSensorEvent(@Valid @RequestBody SensorEventDto request) {
        log.debug("Processing sensor event: type={}", request.getType());

        SensorEventHandler handler = sensorEventHandlers.get(request.getType());
        if (handler != null) {
            handler.handle(request);
        } else {
            throw new IllegalArgumentException(
                    "No handler found for sensor event type: " + request.getType()
            );
        }
    }

    @PostMapping("/hubs")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void collectHubEvent(@Valid @RequestBody HubEventDto request) {
        log.debug("Processing hub event: type={}", request.getType());

        HubEventHandler handler = hubEventHandlers.get(request.getType());
        if (handler != null) {
            handler.handle(request);
        } else {
            throw new IllegalArgumentException(
                    "No handler found for hub event type: " + request.getType()
            );
        };
    }
}

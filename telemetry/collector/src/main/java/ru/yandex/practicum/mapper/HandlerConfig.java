package ru.yandex.practicum.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.entity.HubEventType;
import ru.yandex.practicum.entity.SensorEventType;
import ru.yandex.practicum.mapper.hub.HubEventHandler;
import ru.yandex.practicum.mapper.sensor.SensorEventHandler;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
public class HandlerConfig {

    @Bean
    public Map<SensorEventType, SensorEventHandler> sensorEventHandlers(Set<SensorEventHandler> handlers) {
        return handlers.stream()
                .collect(Collectors.toMap(
                        SensorEventHandler::getSupportedType,
                        Function.identity(),
                        (handler1, handler2) -> {
                            throw new IllegalStateException(
                                    "Duplicate handler for sensor event type: " + handler1.getSupportedType()
                            );
                        }
                ));
    }

    @Bean
    public Map<HubEventType, HubEventHandler> hubEventHandlers(Set<HubEventHandler> handlers) {
        return handlers.stream()
                .collect(Collectors.toMap(
                        HubEventHandler::getSupportedType,
                        Function.identity(),
                        (handler1, handler2) -> {
                            throw new IllegalStateException(
                                    "Duplicate handler for hub event type: " + handler1.getSupportedType()
                            );
                        }
                ));
    }
}

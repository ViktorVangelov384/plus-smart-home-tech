package ru.yandex.practicum.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Getter
@RequiredArgsConstructor
public enum SensorEventType {

    CLIMATE_SENSOR_EVENT("CLIMATE_SENSOR"),
    LIGHT_SENSOR_EVENT("LIGHT_SENSOR"),
    MOTION_SENSOR_EVENT("MOTION_SENSOR"),
    SWITCH_SENSOR_EVENT("SWITCH_SENSOR"),
    TEMPERATURE_SENSOR_EVENT("TEMPERATURE_SENSOR");

    private final String value;

    private static final Map<String, SensorEventType> VALUE_CACHE = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(
                    type -> type.value.toLowerCase(),
                    Function.identity()
            ));

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static SensorEventType fromValue(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Тип нельзя быть пустыр");
        }
        String normalizedValue = value.trim().toLowerCase();
        SensorEventType eventType = VALUE_CACHE.get(normalizedValue);

        if (eventType != null) {
            return eventType;
        }

        String supportedTypes = Arrays.stream(values())
                .map(SensorEventType::getValue)
                .collect(Collectors.joining(", "));

        throw new IllegalArgumentException(
                String.format("Неизвестный тип события сенсора: '%s'. Поддерживаемые типы: %s",
                        value, supportedTypes)
        );
    }
}

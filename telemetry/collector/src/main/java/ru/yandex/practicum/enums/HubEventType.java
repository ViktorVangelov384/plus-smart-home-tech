package ru.yandex.practicum.enums;

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
public enum HubEventType {

    DEVICE_ADDED("DEVICE_ADDED"),
    DEVICE_REMOVED("DEVICE_REMOVED"),
    SCENARIO_ADDED("SCENARIO_ADDED"),
    SCENARIO_REMOVED("SCENARIO_REMOVED");

    private final String value;

    private static final Map<String, HubEventType> LOOKUP = Arrays.stream(values())
            .collect(Collectors.toMap(
                    type -> type.value.toLowerCase(),
                    Function.identity()
            ));

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static HubEventType fromValue(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Hub event type cannot be empty");
        }

        String normalized = value.trim().toLowerCase();
        HubEventType result = LOOKUP.get(normalized);

        if (result != null) {
            return result;
        }

        String supported = Arrays.stream(values())
                .map(HubEventType::getValue)
                .collect(Collectors.joining(", "));
        throw new IllegalArgumentException("Unknown hub event type: " + value);
    }
}


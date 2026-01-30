package ru.yandex.practicum.dto.hub;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.dto.device.DeviceAddedEventDto;
import ru.yandex.practicum.dto.device.DeviceRemovedEventDto;
import ru.yandex.practicum.dto.scenario.ScenarioAddedEventDto;
import ru.yandex.practicum.dto.scenario.ScenarioRemoveEventDto;
import ru.yandex.practicum.enums.HubEventType;

import java.time.Instant;

@Getter
@Setter
@ToString
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = DeviceAddedEventDto.class, name = "DEVICE_ADDED"),
        @JsonSubTypes.Type(value = DeviceRemovedEventDto.class, name =  "DEVICE_REMOVED"),
        @JsonSubTypes.Type(value = ScenarioAddedEventDto.class, name = "SCENARIO_ADDED"),
        @JsonSubTypes.Type(value = ScenarioRemoveEventDto.class, name = "SCENARIO_REMOVED")
})
public abstract class HubEventDto {

    @NotBlank(message = "Идентификатор хаба обязателен")
    private String hubId;

    private Instant timestamp = Instant.now();

    @NotNull(message = "Тип события обязателен")
    public abstract HubEventType getType();

    public boolean isStale(long thresholdInSeconds) {
        if (timestamp == null) {
            return false;
        }
        Instant thresholdTime = Instant.now().minusSeconds(thresholdInSeconds);
        return timestamp.isBefore(thresholdTime);
    }

    public boolean belongsToHub(String hubId) {
        return this.hubId != null && this.hubId.equals(hubId);
    }
}

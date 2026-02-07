package ru.yandex.practicum.dto.scenario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.dto.device.DeviceActionDto;
import ru.yandex.practicum.dto.hub.HubEventDto;
import ru.yandex.practicum.entity.HubEventType;

import java.util.List;

@Getter
@Setter
@ToString(callSuper = true)
public class ScenarioAddedEventDto extends HubEventDto {

    @NotBlank(message = "Название сценария не может быть пустым")
    @Size(min = 2, message = "Название сценария должно содержать минимум 3 символа")
    private String name;

    @NotEmpty(message = "Список условий не может быть пустым")
    private List<ScenarioConditionDto> conditions;

    @NotEmpty(message = "Список действий не может быть пустым")
    private List<DeviceActionDto> actions;

    @Override
    public HubEventType getType() {
        return HubEventType.SCENARIO_ADDED;
    }

    public boolean isValid() {
        return name != null && name.length() >= 3 &&
                conditions != null && !conditions.isEmpty() &&
                actions != null && !actions.isEmpty() &&
                getHubId() != null && !getHubId().isBlank();
    }

}

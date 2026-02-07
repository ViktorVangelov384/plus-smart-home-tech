package ru.yandex.practicum.dto.scenario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.dto.hub.HubEventDto;
import ru.yandex.practicum.entity.HubEventType;

@Getter
@Setter
@ToString(callSuper = true)
public class ScenarioRemoveEventDto extends HubEventDto {

    @NotBlank(message = "Название сценария не может быть пустым")
    @Size(min = 3, message = "Название сценария должно содержать минимум 3 символа")
    private String name;

    @Override
    public HubEventType getType() {
        return HubEventType.SCENARIO_REMOVED;
    }
}

package ru.yandex.practicum.dto.device;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.enums.ActionType;

@Getter
@Setter
@ToString
public class DeviceActionDto {

    @NotBlank(message = "Идентификатор сенсора не может быть пустым")
    private String sensorId;

    @NotNull(message = "Тип действия не может быть null")
    private ActionType type;

    private Integer value;

}
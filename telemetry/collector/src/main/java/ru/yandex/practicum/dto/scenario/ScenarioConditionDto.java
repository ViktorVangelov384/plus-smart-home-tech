package ru.yandex.practicum.dto.scenario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.entity.ConditionType;
import ru.yandex.practicum.entity.Operation;

@Getter
@Setter
@ToString
public class ScenarioConditionDto {

    @NotBlank(message = "Идентификатор сенсора не может быть пустым")
    private String sensorId;

    @NotNull(message = "Тип условия не может быть null")
    private ConditionType type;

    @NotNull(message = "Операция сравнения не может быть null")
    private Operation operation;

    private Integer value;

}

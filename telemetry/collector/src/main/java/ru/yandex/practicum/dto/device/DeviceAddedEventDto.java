package ru.yandex.practicum.dto.device;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.dto.hub.HubEventDto;
import ru.yandex.practicum.entity.DeviceType;
import ru.yandex.practicum.entity.HubEventType;

@Getter
@Setter
@ToString(callSuper = true)
public class DeviceAddedEventDto extends HubEventDto {

    @NotBlank(message = "Идентификатор устройства не может быть пустым")
    private String id;

    @NotNull(message = "Тип устройства не может быть null")
    private DeviceType deviceType;

    @Override
    public HubEventType getType() {
        return HubEventType.DEVICE_ADDED;
    }

    public boolean hasId(String deviceId) {
        return id != null && id.equals(deviceId);
    }

    public boolean isValid() {
        return id != null && !id.isBlank() &&
                deviceType != null &&
                !id.equals(getHubId());
    }
}

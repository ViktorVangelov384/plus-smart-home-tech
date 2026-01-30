package ru.yandex.practicum.dto.device;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.dto.hub.HubEventDto;
import ru.yandex.practicum.enums.HubEventType;

@Getter
@Setter
@ToString(callSuper = true)
public class DeviceRemovedEventDto extends HubEventDto {

    @NotBlank(message = "Идентификатор устройства не может быть пустым")
    private String id;

    @Override
    public HubEventType getType() {
        return HubEventType.DEVICE_REMOVED;
    }

    public boolean hasId(String deviceId) {
        return id != null && id.equals(deviceId);
    }

    public boolean isValid() {
        return id != null && !id.isBlank() &&
                getHubId() != null && !getHubId().isBlank();
    }
}

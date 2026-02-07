package ru.yandex.practicum.mapper;

import com.google.protobuf.Timestamp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.ActionTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.entity.Action;
import ru.yandex.practicum.entity.Scenario;
import ru.yandex.practicum.enums.ActionType;

import java.time.Instant;

@Slf4j
@Component
public final class DeviceActionGrpcConverter {

    public DeviceActionRequest convertToGrpcRequest(Scenario targetScenario,
                                                    String targetHubId,
                                                    String targetDeviceId,
                                                    Action deviceAction) {

        log.debug("Конвертация действия в gRPC: сценарий={}, устройство={}, действие={}",
                targetScenario.getName(), targetDeviceId, deviceAction.getType());

        ActionType actionTypeEnum = deviceAction.getType();

        DeviceActionProto.Builder deviceCommandBuilder = DeviceActionProto.newBuilder()
                .setSensorId(targetDeviceId)
                .setType(convertActionType(actionTypeEnum));

        Integer actionValue = deviceAction.getValue();
        if (actionValue != null) {
            deviceCommandBuilder.setValue(actionValue);
        }

        DeviceActionProto deviceCommand = deviceCommandBuilder.build();

        DeviceActionRequest request = DeviceActionRequest.newBuilder()
                .setHubId(targetHubId)
                .setScenarioName(targetScenario.getName())
                .setAction(deviceCommand)
                .setTimestamp(createTimestamp())
                .build();

        log.trace("Сформирован gRPC запрос: {}", request);
        return request;
    }

    private ActionTypeProto convertActionType(ActionType internalType) {
        switch (internalType) {
            case ACTIVATE:
                return ActionTypeProto.ACTIVATE;
            case DEACTIVATE:
                return ActionTypeProto.DEACTIVATE;
            case INVERSE:
                return ActionTypeProto.INVERSE;
            case SET_VALUE:
                return ActionTypeProto.SET_VALUE;
            default:
                log.warn("Неизвестный тип действия: {}, используется ACTIVATE", internalType);
                return ActionTypeProto.ACTIVATE;
        }
    }

    private Timestamp createTimestamp() {
        Instant currentTime = Instant.now();
        return Timestamp.newBuilder()
                .setSeconds(currentTime.getEpochSecond())
                .setNanos(currentTime.getNano())
                .build();
    }
}
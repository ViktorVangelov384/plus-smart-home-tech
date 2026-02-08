package ru.yandex.practicum.service;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.exception.GrpcCommunicationException;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc;

@Slf4j
@Service
@RequiredArgsConstructor
public class HubRouterGrpcClient {

    private final HubRouterControllerGrpc.HubRouterControllerBlockingStub grpcChannel;

    public void transmitDeviceCommand(DeviceActionRequest actionCommand) {
        String hubId = actionCommand.getHubId();
        String activeScenario = actionCommand.getScenarioName();
        String scenarioId = actionCommand.getAction().getSensorId();

        log.info("Инициирую передачу команды. Хаб: {}, Сценарий: {}, Устройство: {}",
                hubId, activeScenario, scenarioId);

        try {
            grpcChannel.handleDeviceAction(actionCommand);

            log.debug("Команда успешно доставлена на хаб {}", hubId);

        } catch (StatusRuntimeException grpcError) {
            Status errorStatus = grpcError.getStatus();
            log.error("Сбой gRPC соединения. Код: {}, Описание: {}",
                    errorStatus.getCode(), errorStatus.getDescription(), grpcError);

            throw new GrpcCommunicationException(
                    String.format("Ошибка передачи команды: %s", errorStatus.getDescription()),
                    grpcError
            );

        } catch (Exception unexpectedError) {
            log.error("Непредвиденная проблема при отправке команды", unexpectedError);
            throw new GrpcCommunicationException("Сбой при передаче команды устройству", unexpectedError);
        }
    }

    public boolean verifyConnection() {
        log.trace("Выполняется проверка доступности сервера маршрутизации");
        try {
            var channel = (io.grpc.ManagedChannel) grpcChannel.getChannel();
            boolean isShutdown = channel.isShutdown();
            boolean isTerminated = channel.isTerminated();

            if (isShutdown || isTerminated) {
                log.warn("gRPC канал недоступен: shutdown={}, terminated={}",
                        isShutdown, isTerminated);
                return false;
            }

            log.debug("Соединение с сервером маршрутизации активно");
            return true;

        } catch (StatusRuntimeException e) {
            log.warn("Сервер маршрутизации недоступен: {}", e.getStatus().getDescription());
            return false;
        } catch (Exception e) {
            log.warn("Ошибка при проверке соединения с сервером маршрутизации", e);
            return false;
        }
    }

}

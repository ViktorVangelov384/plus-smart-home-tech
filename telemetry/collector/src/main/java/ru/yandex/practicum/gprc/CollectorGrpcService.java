package ru.yandex.practicum.gprc;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.yandex.practicum.gprc.hub.proccessor.GrpcHubProcessor;
import ru.yandex.practicum.gprc.sensor.processor.GrpcSensorProcessor;
import ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerGrpc;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class CollectorGrpcService extends CollectorControllerGrpc.CollectorControllerImplBase {

    private final GrpcSensorProcessor sensorProcessor;
    private final GrpcHubProcessor hubProcessor;

    @Override
    public void collectSensorEvent(SensorEventProto request, StreamObserver<Empty> responseObserver) {
        try {
            log.info("Получено sensor событие: ID={}, Hub={}", request.getId(), request.getHubId());

            sensorProcessor.processSensorEvent(request);

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();

        } catch (IllegalArgumentException e) {
            log.warn("Клиентская ошибка: {}", e.getMessage());
            responseObserver.onError(new StatusRuntimeException(
                    Status.INVALID_ARGUMENT.withDescription(e.getMessage())
            ));
        } catch (Exception e) {
            log.error("Серверная ошибка: {}", e.getMessage(), e);
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL.withDescription("Внутренняя ошибка сервера")
            ));
        }
    }

    @Override
    public void collectHubEvent(HubEventProto request, StreamObserver<Empty> responseObserver) {
        try {
            log.info("Получено hub событие: Hub={}", request.getHubId());

            hubProcessor.processHubEvent(request);

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();

        } catch (IllegalArgumentException e) {
            log.debug("Клиентская ошибка: {}", e.getMessage());
            responseObserver.onError(new StatusRuntimeException(
                    Status.INVALID_ARGUMENT.withDescription(e.getMessage())
            ));
        } catch (Exception e) {
            log.error("Серверная ошибка: {}", e.getMessage(), e);
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL.withDescription("Внутренняя ошибка сервера")
            ));
        }
    }
}
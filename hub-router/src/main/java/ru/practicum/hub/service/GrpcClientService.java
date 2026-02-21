package ru.practicum.hub.service;

import com.google.protobuf.Empty;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerGrpc;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;

@Slf4j
@Service
public class GrpcClientService {
    @Value("${grpc.collector.host:localhost}")
    private String collectorHost;
    @Value("${grpc.collector.port:59091}")
    private int collectorPort;
    @Value("${hub.id:hub-1}")
    private String hubId;
    private ManagedChannel channel;
    private CollectorControllerGrpc.CollectorControllerBlockingStub blockingStub;

    @PostConstruct
    public void init() {
        channel = ManagedChannelBuilder.forAddress(collectorHost, collectorPort).usePlaintext().build();
        blockingStub = CollectorControllerGrpc.newBlockingStub(channel);
        log.info("gRPC клиент инициализирован: {}:{}, Hub ID: {}", collectorHost, collectorPort, hubId);
    }

    public void sendEvent(SensorEventProto event) {
        if (event == null) {
            log.warn("Попытка отправить null событие");
            return;
        }
        try {
            SensorEventProto eventWithHub = event.toBuilder().setHubId(hubId).build();
            log.debug("Отправка события: {}, hub: {}", event.getId(), hubId);
            Empty response = blockingStub.collectSensorEvent(eventWithHub);
            log.trace("Событие успешно отправлено: {}", event.getId());
        } catch (StatusRuntimeException e) {
            handleGrpcError(event, e);
        } catch (Exception e) {
            log.error("Неожиданная ошибка при отправке события {}: {}", event.getId(), e.getMessage(), e);
        }
    }

    private void handleGrpcError(SensorEventProto event, StatusRuntimeException e) {
        Status status = e.getStatus();
        switch (status.getCode()) {
            case UNAVAILABLE:
                log.error("Сервис коллектора недоступен при отправке события {}: {}", event.getId(), status.getDescription());
                break;
            case DEADLINE_EXCEEDED:
                log.warn("Таймаут при отправке события {}: {}", event.getId(), status.getDescription());
                break;
            case PERMISSION_DENIED:
                log.error("Ошибка авторизации при отправке события {}: {}", event.getId(), status.getDescription());
                break;
            default:
                log.error("gRPC ошибка при отправке события {}: {} ({})", event.getId(), status.getDescription(), status.getCode());
        }
    }

    @PreDestroy
    public void shutdown() {
        if (channel != null && !channel.isShutdown()) {
            channel.shutdown();
            log.info("gRPC канал закрыт");
        }
    }
}
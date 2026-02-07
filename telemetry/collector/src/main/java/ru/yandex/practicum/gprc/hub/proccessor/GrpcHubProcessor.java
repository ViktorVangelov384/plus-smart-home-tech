package ru.yandex.practicum.gprc.hub.proccessor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.gprc.hub.service.HubEventHandler;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GrpcHubProcessor {

    private final Map<HubEventProto.PayloadCase, HubEventHandler> eventHandlers;

    public GrpcHubProcessor(List<HubEventHandler> hubHandlers) {
        log.info("Инициализация GrpcHubProcessor. Получено обработчиков: {}", hubHandlers.size());

        this.eventHandlers = hubHandlers.stream()
                .collect(Collectors.toMap(
                        HubEventHandler::getMessageType,
                        Function.identity(),
                        (existing, replacement) -> {
                            String errorMessage = String.format(
                                    "Обнаружены дублирующиеся обработчики для типа события %s: %s и %s",
                                    existing.getMessageType(),
                                    existing.getClass().getSimpleName(),
                                    replacement.getClass().getSimpleName()
                            );
                            log.error(errorMessage);
                            throw new IllegalStateException(errorMessage);
                        }
                ));

        log.info("GrpcHubProcessor успешно инициализирован. Поддерживаемых типов событий: {}",
                eventHandlers.size());
    }

    public void processHubEvent(HubEventProto event) {
        log.info("Начало обработки Hub-события. Hub ID: {}", event.getHubId());

        try {
            validateHubEvent(event);

            HubEventProto.PayloadCase eventType = event.getPayloadCase();
            HubEventHandler handler = getHandlerForEventType(eventType);

            log.debug("Найден обработчик для типа {}: {}",
                    eventType, handler.getClass().getSimpleName());

            handler.handle(event);

            log.info("Hub-событие успешно обработано. Тип: {}, Hub ID: {}",
                    eventType, event.getHubId());

        } catch (IllegalArgumentException e) {
            log.warn("Ошибка валидации Hub-события: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Критическая ошибка при обработке Hub-события для хаба {}: {}",
                    event.getHubId(), e.getMessage(), e);
            throw new RuntimeException("Не удалось обработать Hub-событие: " + e.getMessage(), e);
        }
    }

    private void validateHubEvent(HubEventProto event) {
        if (event == null) {
            throw new IllegalArgumentException("HubEventProto не может быть null");
        }

        if (event.getPayloadCase() == HubEventProto.PayloadCase.PAYLOAD_NOT_SET) {
            throw new IllegalArgumentException("Тип события не определен (PAYLOAD_NOT_SET)");
        }

        if (event.getHubId() == null || event.getHubId().isBlank()) {
            throw new IllegalArgumentException("Идентификатор хаба (hubId) не может быть null или пустым");
        }

        if (event.getTimestamp() == null) {
            throw new IllegalArgumentException("Временная метка (timestamp) не может быть null");
        }
    }

    private HubEventHandler getHandlerForEventType(HubEventProto.PayloadCase eventType) {
        HubEventHandler handler = eventHandlers.get(eventType);

        if (handler == null) {
            String errorMessage = String.format(
                    "Не найден обработчик для типа события: %s. Доступные типы: %s",
                    eventType,
                    eventHandlers.keySet()
            );
            log.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        return handler;
    }
}

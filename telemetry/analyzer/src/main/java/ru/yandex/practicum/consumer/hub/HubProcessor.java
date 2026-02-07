package ru.yandex.practicum.consumer.hub;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.consumer.hub.handler.EventHandler;
import ru.yandex.practicum.consumer.processor.KafkaMessageProcessor;
import ru.yandex.practicum.consumer.processor.ProcessorProperties;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

import java.util.List;
import java.util.Properties;

@Slf4j
@Component
public class HubProcessor extends KafkaMessageProcessor<HubEventAvro> {

    private final Properties consumerConfiguration;
    private final List<EventHandler> eventHandlers;

    public HubProcessor(ProcessorProperties processorConfig,
                        @Qualifier("hubEventClientConfig")
                              Properties kafkaConsumerConfig,
                              List<EventHandler> registeredHandlers) {
        super(processorConfig);
        this.consumerConfiguration = kafkaConsumerConfig;
        this.eventHandlers = registeredHandlers;
        log.info("Создан процессор событий хаба с {} обработчиками", registeredHandlers.size());
    }

    @Override
    protected Consumer<String, HubEventAvro> initializeConsumer() {
        log.debug("Инициализация Kafka потребителя для событий хаба");
        return new KafkaConsumer<>(consumerConfiguration);
    }

    @Override
    protected void handleMessage(HubEventAvro hubEvent) {
        String sourceHubId = hubEvent.getHubId();
        Object eventPayload = hubEvent.getPayload();
        String payloadTypeName = eventPayload.getClass().getSimpleName();

        log.debug("Обработка события хаба. Идентификатор: {}, Тип события: {}",
                sourceHubId, payloadTypeName);

        processEventWithHandlers(sourceHubId, eventPayload, payloadTypeName);
    }

    @Override
    protected String getTargetTopic() {
        return "telemetry.hubs.v1";
    }

    @Override
    protected String getProcessorId() {
        return "HubProcessor";
    }

    private void processEventWithHandlers(String hubId, Object payload, String payloadType) {
        for (EventHandler currentHandler : eventHandlers) {
            if (currentHandler.canHandle(payload)) {
                executeEventHandler(currentHandler, hubId, payload, payloadType);
                return;
            }
        }

        logUnhandledEvent(payloadType);
    }

    private void executeEventHandler(EventHandler handler,
                                     String hubId,
                                     Object payload,
                                     String payloadType) {
        String handlerName = handler.getClass().getSimpleName();
        log.trace("Обработчик '{}' выбран для события типа '{}'", handlerName, payloadType);

        try {
            handler.handle(hubId, payload);
        } catch (Exception handlingError) {
            log.error("Ошибка в обработчике '{}' для события типа '{}'",
                    handlerName, payloadType, handlingError);
        }
    }

    private void logUnhandledEvent(String payloadType) {
        log.warn("Не обнаружен подходящий обработчик для типа события: {}", payloadType);
    }

    public void displayHandlersInfo() {
        log.info("Диагностика процессора событий хаба. Зарегистрировано обработчиков: {}",
                eventHandlers.size());

        eventHandlers.forEach(handler ->
                log.debug("  Обработчик: {}", handler.getClass().getSimpleName())
        );
    }
}

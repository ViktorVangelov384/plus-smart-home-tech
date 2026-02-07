package ru.yandex.practicum.consumer.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;

import java.time.Duration;
import java.util.Collections;

@Slf4j
public abstract class KafkaMessageProcessor<T> implements Runnable {

    protected final ProcessorProperties properties;
    private volatile boolean active = true;

    protected KafkaMessageProcessor(ProcessorProperties properties) {
        this.properties = properties;
        log.debug("Создан обработчик {} с конфигурацией: {}",
                getProcessorId(), properties);
    }

    //Метод для создания экземпляра потребителя Kafka
    protected abstract Consumer<String, T> initializeConsumer();

    // Обрабатывает единичное сообщение
    protected abstract void handleMessage(T messageContent);

    protected abstract String getTargetTopic();

    protected abstract String getProcessorId();

    @Override
    public void run() {
        log.info("Активация обработчика {} для топика {}",
                getProcessorId(), getTargetTopic());

        try (Consumer<String, T> messageConsumer = initializeConsumer()) {
            messageConsumer.subscribe(Collections.singletonList(getTargetTopic()));
            log.info("Обработчик {} успешно подписан на топик {}",
                    getProcessorId(), getTargetTopic());

            while (active) {
                executeProcessingCycle(messageConsumer);
            }
        } catch (Exception criticalError) {
            log.error("Критический сбой в работе обработчика {}",
                    getProcessorId(), criticalError);
        } finally {
            log.info("Обработчик {} завершил работу", getProcessorId());
        }
    }

    private void executeProcessingCycle(Consumer<String, T> consumer) {
        try {
            ConsumerRecords<String, T> receivedMessages =
                    consumer.poll(Duration.ofMillis(properties.getPollingTimeout()));

            if (!receivedMessages.isEmpty()) {
                log.debug("Получено {} сообщений обработчиком {}",
                        receivedMessages.count(), getProcessorId());

                processMessageBatch(receivedMessages);
                commitConsumerOffsets(consumer);
            }
        } catch (Exception pollingError) {
            log.error("Ошибка при опросе Kafka обработчиком {}",
                    getProcessorId(), pollingError);
        }
    }

    private void processMessageBatch(ConsumerRecords<String, T> messages) {
        messages.forEach(message -> {
            try {
                handleMessage(message.value());
            } catch (Exception processingError) {
                log.error("Сбой обработки сообщения обработчиком {}",
                        getProcessorId(), processingError);
            }
        });
    }

    private void commitConsumerOffsets(Consumer<String, T> consumer) {
        try {
            consumer.commitSync();
            log.trace("Смещения обработчика {} подтверждены", getProcessorId());
        } catch (Exception commitError) {
            log.error("Не удалось подтвердить смещения обработчика {}",
                    getProcessorId(), commitError);
        }
    }

    public void terminate() {
        log.info("Инициирована остановка обработчика {}", getProcessorId());
        active = false;
    }
}

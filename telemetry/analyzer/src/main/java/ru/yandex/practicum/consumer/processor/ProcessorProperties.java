package ru.yandex.practicum.consumer.processor;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "analyzer.kafka.processor")
public class ProcessorProperties {

    //Ожидание kafka в миллисекундах
    private int pollingTimeout = 1000;

    //Подтверждение обработки сообщений
    private boolean autoCommit = false;

    private String autoOffsetReset = "earliest";

    //Макс кол-во сообщений за один цикл
    private int maxPollRecords = 500;

    //Максимальное время неактивности потребителя
    private int sessionTimeoutMs = 10000;

    //Периодичность отправки сигналов активности в миллисекундах
    private int heartbeatIntervalMs = 3000;

    //Интервал между последовательными опросами в миллисекундах
    private int maxPollIntervalMs = 300000;

    //Автоматическое создание топиков
    private boolean allowAutoCreateTopics = false;

    public void validateConfiguration() {
        if (pollingTimeout <= 0) {
            throw new IllegalArgumentException(
                    "Параметр pollingTimeout должен иметь положительное значение"
            );
        }
        if (maxPollRecords <= 0) {
            throw new IllegalArgumentException(
                    "Параметр maxPollRecords должен иметь положительное значение"
            );
        }
        validateAutoOffsetReset();
    }

    private void validateAutoOffsetReset() {
        if (!"earliest".equals(autoOffsetReset) &&
                !"latest".equals(autoOffsetReset) &&
                !"none".equals(autoOffsetReset)) {
            throw new IllegalArgumentException(
                    "Параметр autoOffsetReset может принимать значения: earliest, latest, none"
            );
        }
    }
}

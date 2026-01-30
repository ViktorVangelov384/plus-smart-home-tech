package ru.yandex.practicum.producer;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.config.ConfigKafka;

import java.time.Duration;
import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Component
public class EventProducer implements AutoCloseable {
    private static final Duration SHUTDOWN_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration SEND_TIMEOUT = Duration.ofSeconds(5);

    private final KafkaProducer<String, SpecificRecordBase> producer;
    private final Map<ConfigKafka.TopicType, String> topics;

    public EventProducer(ConfigKafka configKafka) {
        log.info("Инициализация EventProducer...");

        validateConfig(configKafka);
        this.topics = createTopicMapping(configKafka);
        this.producer = createKafkaProducer(configKafka);

        log.info("EventProducer инициализирован для топиков: {}", topics);
    }

    private void validateConfig(ConfigKafka configKafka) {
        if (configKafka == null || configKafka.getProducer() == null) {
            throw new IllegalArgumentException("Конфигурация Kafka не может быть null");
        }
    }

    private Map<ConfigKafka.TopicType, String> createTopicMapping(ConfigKafka configKafka) {
        Map<ConfigKafka.TopicType, String> topicMap = new EnumMap<>(ConfigKafka.TopicType.class);

        for (ConfigKafka.TopicType topicType : ConfigKafka.TopicType.values()) {
            String topicName = configKafka.getProducer().getTopicName(topicType);
            topicMap.put(topicType, topicName);
        }

        return topicMap;
    }

    private KafkaProducer<String, SpecificRecordBase> createKafkaProducer(ConfigKafka configKafka) {
        var properties = configKafka.getProducer().getProperties();

        Map<String, Object> config = new java.util.HashMap<>();
        for (String key : properties.stringPropertyNames()) {
            config.put(key, properties.getProperty(key));
        }

        validateProducerConfig(config);
        return new KafkaProducer<>(config);
    }

    private void validateProducerConfig(Map<String, Object> config) {
        if (!config.containsKey("bootstrap.servers")) {
            throw new IllegalArgumentException("Отсутствует обязательный параметр: bootstrap.servers");
        }
        if (!config.containsKey("key.serializer")) {
            throw new IllegalArgumentException("Отсутствует обязательный параметр: key.serializer");
        }
        if (!config.containsKey("value.serializer")) {
            throw new IllegalArgumentException("Отсутствует обязательный параметр: value.serializer");
        }
    }

    public void send(SpecificRecordBase event, String hubId, Instant timestamp,
                     ConfigKafka.TopicType topicType) {

        validateEvent(event, hubId, topicType);

        String topic = getTopic(topicType);
        ProducerRecord<String, SpecificRecordBase> record = createRecord(topic, hubId, event, timestamp);

        sendAsyncWithCallback(record, topic, hubId);
    }

    private void validateEvent(SpecificRecordBase event, String hubId, ConfigKafka.TopicType topicType) {
        if (event == null) {
            throw new IllegalArgumentException("Событие не может быть null");
        }
        if (hubId == null || hubId.isBlank()) {
            throw new IllegalArgumentException("Идентификатор хаба не может быть пустым");
        }
        if (topicType == null) {
            throw new IllegalArgumentException("Тип топика не может быть null");
        }
    }

    private String getTopic(ConfigKafka.TopicType topicType) {
        String topic = topics.get(topicType);
        if (topic == null) {
            throw new IllegalArgumentException("Топик не найден для типа: " + topicType);
        }
        return topic;
    }

    private ProducerRecord<String, SpecificRecordBase> createRecord(
            String topic, String hubId, SpecificRecordBase event, Instant timestamp) {

        long messageTimestamp = timestamp != null
                ? timestamp.toEpochMilli()
                : System.currentTimeMillis();

        return new ProducerRecord<>(
                topic,
                null,
                messageTimestamp,
                hubId,
                event
        );
    }

    private void sendAsyncWithCallback(ProducerRecord<String, SpecificRecordBase> record,
                                       String topic, String hubId) {

        producer.send(record, (metadata, exception) -> {
            if (exception != null) {
                log.error("Ошибка отправки в Kafka - Топик: {}, Хаб: {}, Ошибка: {}",
                        topic, hubId, exception.getMessage(), exception);
            } else {
                log.debug("Сообщение отправлено - Топик: {}, Партиция: {}, Offset: {}, Хаб: {}",
                        metadata.topic(), metadata.partition(), metadata.offset(), hubId);
            }
        });

        producer.flush();
    }

    @Override
    public void close() {
        if (producer != null) {
            try {
                log.info("Завершение работы EventProducer...");
                producer.flush();
                producer.close(SHUTDOWN_TIMEOUT);
                log.info("Kafka продюсер успешно закрыт");
            } catch (Exception e) {
                log.warn("Ошибка при закрытии Kafka продюсера: {}", e.getMessage());
            }
        }
    }
}

package ru.yandex.practicum.config;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import java.util.EnumMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Setter
@Getter
@ConfigurationProperties(prefix = "collector.kafka")
public class ConfigKafka {

    private ProducerConfig producer;

    @ConstructorBinding
    public ConfigKafka(ProducerConfig producer) {
        this.producer = producer;
    }

    @Getter
    @Setter
    public static class ProducerConfig {
        private final Properties properties;

        private final EnumMap<TopicType, String> topics;

        public ProducerConfig(Properties properties, Map<String, String> topics) {
            this.properties = properties != null ? properties : new Properties();
            this.topics = new EnumMap<>(TopicType.class);

            if (topics != null) {
                topics.forEach((key, value) -> {
                    TopicType topicType = TopicType.from(key);
                    if (topicType != null) {
                        this.topics.put(topicType, value);
                    } else {
                        log.warn("Неизвестный тип топика в конфигурации: '{}'. Пропускаем.", key);
                    }
                });
            }

            validateTopicsConfiguration();
        }

        private void validateTopicsConfiguration() {
            for (TopicType requiredTopic : TopicType.values()) {
                if (!topics.containsKey(requiredTopic)) {
                    throw new IllegalStateException(
                            String.format("Обязательный топик '%s' не сконфигурирован", requiredTopic)
                    );
                }
            }
        }

        public String getTopicName(TopicType topicType) {
            String topicName = topics.get(topicType);
            if (topicName == null) {
                throw new IllegalArgumentException(
                        String.format("Топик типа '%s' не сконфигурирован", topicType)
                );
            }
            return topicName;
        }
    }

    public enum TopicType {
        SENSORS_EVENTS,
        HUBS_EVENTS;

        private static final Map<String, TopicType> NAME_MAP = Stream.of(values())
                .collect(Collectors.toUnmodifiableMap(
                        type -> type.name().toLowerCase().replace("_", "-"),
                        type -> type
                ));

        public static TopicType from(String type) {
            if (type == null || type.isBlank()) {
                return null;
            }

            String normalized = type.trim().toLowerCase();

            TopicType result = NAME_MAP.get(normalized);

            if (result == null) {
                try {
                    result = TopicType.valueOf(normalized.toUpperCase().replace("-", "_"));
                } catch (IllegalArgumentException e) {
                    log.debug("Не удалось преобразовать '{}' в TopicType", type);
                }
            }

            return result;
        }

        public String toConfigName() {
            return name().toLowerCase().replace("_", "-");
        }
    }
}

package ru.yandex.practicum.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.consumer.processor.ProcessorProperties;
import ru.yandex.practicum.deserializer.HubEventRouterDeserializer;
import ru.yandex.practicum.deserializer.SensorsSnapshotDeserializer;

import java.util.Properties;

@Slf4j
@Configuration
public class KafkaClientConfig {

    @Value("${kafka.bootstrap-servers}")
    private String kafkaServers;

    @Value("${analyzer.kafka.consumer.hubs.group-id}")
    private String hubEventsGroup;

    @Value("${analyzer.kafka.consumer.snapshots.group-id}")
    private String snapshotEventsGroup;

    @Bean
    public Properties hubEventClientConfig(ProcessorProperties processorConfig) {
        log.debug("Подготовка конфигурации Kafka клиента для событий хаба");

        Properties clientConfiguration = buildCommonClientConfig(processorConfig);
        configureHubEventSpecifics(clientConfiguration);

        log.info("Конфигурация клиента событий хаба готова. Группа: {}, Топик: {}",
                hubEventsGroup, "telemetry.hubs.v1");
        return clientConfiguration;
    }

    @Bean
    public Properties snapshotClientConfig(ProcessorProperties processorConfig) {
        log.debug("Подготовка конфигурации Kafka клиента для снимков состояний");

        Properties clientConfiguration = buildCommonClientConfig(processorConfig);
        configureSnapshotSpecifics(clientConfiguration);

        log.info("Конфигурация клиента снимков состояний готова. Группа: {}, Топик: {}",
                snapshotEventsGroup, "telemetry.snapshots.v1");
        return clientConfiguration;
    }

    private Properties buildCommonClientConfig(ProcessorProperties processorConfig) {
        Properties configuration = new Properties();

        configuration.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServers);
        configuration.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class.getName());
        configuration.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,
                String.valueOf(processorConfig.isAutoCommit()));

        configuration.setProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG,
                String.valueOf(processorConfig.getMaxPollRecords()));
        configuration.setProperty(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG,
                String.valueOf(processorConfig.getSessionTimeoutMs()));
        configuration.setProperty(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG,
                String.valueOf(processorConfig.getHeartbeatIntervalMs()));
        configuration.setProperty(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG,
                String.valueOf(processorConfig.getMaxPollIntervalMs()));

        configuration.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                processorConfig.getAutoOffsetReset());
        configuration.setProperty(ConsumerConfig.ALLOW_AUTO_CREATE_TOPICS_CONFIG,
                String.valueOf(processorConfig.isAllowAutoCreateTopics()));

        return configuration;
    }

    private void configureHubEventSpecifics(Properties configuration) {
        configuration.setProperty(ConsumerConfig.GROUP_ID_CONFIG, hubEventsGroup);
        configuration.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                HubEventRouterDeserializer.class.getName());
        configuration.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    }

    private void configureSnapshotSpecifics(Properties configuration) {
        configuration.setProperty(ConsumerConfig.GROUP_ID_CONFIG, snapshotEventsGroup);
        configuration.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                SensorsSnapshotDeserializer.class.getName());
        configuration.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
    }
}

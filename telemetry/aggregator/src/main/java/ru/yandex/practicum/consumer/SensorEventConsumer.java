package ru.yandex.practicum.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

import java.time.Duration;
import java.util.Collections;

@Slf4j
@Component
public class SensorEventConsumer implements AutoCloseable{

    private static final String TOPIC = "telemetry.sensors.v1";
    private static final Duration POLL_TIMEOUT = Duration.ofMillis(100);

    private final Consumer<String, SensorEventAvro> consumer;

    public SensorEventConsumer(Consumer<String, SensorEventAvro> consumer) {
        this.consumer = consumer;
        this.consumer.subscribe(Collections.singletonList(TOPIC));
        log.info("Подписан на топик: {}", TOPIC);
    }

    public ConsumerRecords<String, SensorEventAvro> pollEvents() {
        return consumer.poll(POLL_TIMEOUT);
    }

    @Override
    public void close() {
        consumer.close();
        log.info("Consumer закрыт");
    }
}

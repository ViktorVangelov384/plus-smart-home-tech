package ru.yandex.practicum.starter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.consumer.SensorEventConsumer;
import ru.yandex.practicum.publisher.SnapshotsPublisher;
import ru.yandex.practicum.service.AggregatorService;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregatorStarter{

    private final SensorEventConsumer consumer;
    private final SnapshotsPublisher publisher;
    private final AggregatorService aggregatorService;

    public void run() {
        log.info("Запуск процесса агрегации данных");

        try {
            while (true) {
                ConsumerRecords<String, ?> records = consumer.pollEvents();

                if (!records.isEmpty()) {
                    aggregatorService.processRecords(records)
                            .forEach(publisher::publishSnapshot);
                }
            }
        } catch (Exception e) {
            log.error("Ошибка в процессе агрегации", e);
        } finally {
            shutdown();
        }
    }

    private void shutdown() {
        log.info("Завершение работы процессора");
        consumer.close();
        publisher.close();
    }
}
package ru.yandex.practicum.publisher;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

@Slf4j
@Component
public class SnapshotsPublisher {

    private static final String TOPIC = "telemetry.snapshots.v1";

    private final Producer<String, SensorsSnapshotAvro> producer;

    public SnapshotsPublisher(Producer<String, SensorsSnapshotAvro> producer) {
        this.producer = producer;
    }

    public void publishSnapshot(SensorsSnapshotAvro snapshot) {
        String hubId = snapshot.getHubId().toString();

        ProducerRecord<String, SensorsSnapshotAvro> record =
                new ProducerRecord<>(TOPIC, hubId, snapshot);

        producer.send(record, (metadata, exception) -> {
            if (exception != null) {
                log.error("Ошибка отправки снапшота для хаба {}", hubId, exception);
            } else {
                log.debug("Снапшот отправлен: хаб={}, partition={}, offset={}",
                        hubId, metadata.partition(), metadata.offset());
            }
        });
    }

    public void close() {
        producer.flush();
        producer.close();
        log.info("Producer закрыт");
    }
}

package ru.yandex.practicum.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AggregatorService {

    private final Map<String, SensorsSnapshotAvro> snapshots = new ConcurrentHashMap<>();

    public List<SensorsSnapshotAvro> processRecords(ConsumerRecords<String, ?> records) {
        List<SensorsSnapshotAvro> updatedSnapshots = new ArrayList<>();

        records.forEach(record -> {
            if (record.value() instanceof SensorEventAvro) {
                SensorEventAvro event = (SensorEventAvro) record.value();
                updateState(event).ifPresent(updatedSnapshots::add);
            }
        });

        return updatedSnapshots;
    }

    public Optional<SensorsSnapshotAvro> updateState(SensorEventAvro event) {
        String hubId = event.getHubId();
        String deviceId = event.getId().toString();

        SensorsSnapshotAvro snapshot = snapshots.computeIfAbsent(hubId,
                id -> SensorsSnapshotAvro.newBuilder()
                        .setHubId(hubId)
                        .setTimestamp(event.getTimestamp())
                        .setSensorsState(new HashMap<>())
                        .build()
        );

        Map<String, SensorStateAvro> sensorsState = new HashMap<>(snapshot.getSensorsState());
        SensorStateAvro oldState = sensorsState.get(deviceId);

        if (oldState != null) {
            if (oldState.getTimestamp().compareTo(event.getTimestamp()) > 0 ||
                    (oldState.getData() instanceof SpecificRecord &&
                            event.getPayload() instanceof SpecificRecord &&
                            oldState.getData().equals(event.getPayload()))) {
                return Optional.empty();
            }
        }

        SensorStateAvro newState = SensorStateAvro.newBuilder()
                .setTimestamp(event.getTimestamp())
                .setData(event.getPayload())
                .build();

        sensorsState.put(deviceId, newState);

        SensorsSnapshotAvro updatedSnapshot = SensorsSnapshotAvro.newBuilder()
                .setHubId(hubId)
                .setTimestamp(event.getTimestamp())
                .setSensorsState(sensorsState)
                .build();

        snapshots.put(hubId, updatedSnapshot);

        log.debug("Обновлен снапшот для хаба {}: устройство {}", hubId, deviceId);
        return Optional.of(updatedSnapshot);
    }
}

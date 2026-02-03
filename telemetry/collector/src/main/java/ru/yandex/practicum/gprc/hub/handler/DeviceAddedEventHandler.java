package ru.yandex.practicum.gprc.hub.handler;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.producer.EventProducer;
import ru.yandex.practicum.grpc.telemetry.event.DeviceAddedEventProto;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

import java.time.Instant;

@Slf4j
@Component("grpcDeviceAddedEventHandler")
public class DeviceAddedEventHandler extends AbstractHubEventHandler {

    public DeviceAddedEventHandler(EventProducer producer) {
        super(producer);
    }

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.DEVICE_ADDED;
    }

    @Override
    protected SpecificRecordBase mapToAvro(HubEventProto event) {
        DeviceAddedEventProto deviceEvent = event.getDeviceAdded();

        DeviceAddedEventAvro deviceAvro = DeviceAddedEventAvro.newBuilder()
                .setId(deviceEvent.getId())
                .setType(DeviceTypeAvro.valueOf(deviceEvent.getType().name()))
                .build();

        Instant eventInstant = Instant.ofEpochSecond(
                event.getTimestamp().getSeconds(),
                event.getTimestamp().getNanos()
        );

        HubEventAvro hubEvent = HubEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setTimestamp(eventInstant)
                .setPayload(deviceAvro)
                .build();

        log.debug("Создано HubEventAvro: hubId={}, deviceId={}",
                event.getHubId(), deviceEvent.getId());
        return hubEvent;
    }
}
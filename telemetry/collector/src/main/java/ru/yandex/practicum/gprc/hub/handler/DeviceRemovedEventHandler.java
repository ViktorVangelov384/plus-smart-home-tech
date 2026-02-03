package ru.yandex.practicum.gprc.hub.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.DeviceRemovedEventProto;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.producer.EventProducer;

import java.time.Instant;

@Slf4j
@Component("grpcDeviceRemovedEventHandler")
public class DeviceRemovedEventHandler extends AbstractHubEventHandler {

    public DeviceRemovedEventHandler(EventProducer producer) {
        super(producer);
    }

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.DEVICE_REMOVED;
    }

    @Override
    protected HubEventAvro mapToAvro(HubEventProto event) {
        DeviceRemovedEventProto deviceEvent = event.getDeviceRemoved();

        DeviceRemovedEventAvro deviceAvro = DeviceRemovedEventAvro.newBuilder()
                .setId(deviceEvent.getId())
                .build();

        Instant eventInstant = Instant.ofEpochSecond(
                event.getTimestamp().getSeconds(),
                event.getTimestamp().getNanos()
        );

        return HubEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setTimestamp(eventInstant)
                .setPayload(deviceAvro)
                .build();
    }
}

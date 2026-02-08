package ru.yandex.practicum.gprc.sensor.mapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.enums.SensorEventType;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;

import java.util.EnumMap;
import java.util.Map;

@Slf4j
@Component
public class SensorEventTypeMapper {

    private static final Map<SensorEventProto.PayloadCase, SensorEventType> PAYLOAD_TO_EVENT_TYPE_MAP;

    static {
        PAYLOAD_TO_EVENT_TYPE_MAP = new EnumMap<>(SensorEventProto.PayloadCase.class);
        PAYLOAD_TO_EVENT_TYPE_MAP.put(SensorEventProto.PayloadCase.MOTION_SENSOR,
                SensorEventType.MOTION_SENSOR_EVENT);
        PAYLOAD_TO_EVENT_TYPE_MAP.put(SensorEventProto.PayloadCase.TEMPERATURE_SENSOR,
                SensorEventType.TEMPERATURE_SENSOR_EVENT);
        PAYLOAD_TO_EVENT_TYPE_MAP.put(SensorEventProto.PayloadCase.LIGHT_SENSOR,
                SensorEventType.LIGHT_SENSOR_EVENT);
        PAYLOAD_TO_EVENT_TYPE_MAP.put(SensorEventProto.PayloadCase.CLIMATE_SENSOR,
                SensorEventType.CLIMATE_SENSOR_EVENT);
        PAYLOAD_TO_EVENT_TYPE_MAP.put(SensorEventProto.PayloadCase.SWITCH_SENSOR,
                SensorEventType.SWITCH_SENSOR_EVENT);
    }

    public SensorEventType fromProto(SensorEventProto sensorEventProto) {
        log.debug("Определение типа события для protobuf сообщения");

        validateInput(sensorEventProto);

        SensorEventProto.PayloadCase payloadCase = sensorEventProto.getPayloadCase();
        log.debug("Определен PayloadCase: {} для события ID: {}", payloadCase, sensorEventProto.getId());

        return mapPayloadCaseToEventType(payloadCase, sensorEventProto.getId());
    }

    private void validateInput(SensorEventProto sensorEventProto) {
        if (sensorEventProto == null) {
            log.error("Входное protobuf сообщение не может быть null");
            throw new IllegalArgumentException("SensorEventProto не может быть null");
        }

        if (sensorEventProto.getId() == null || sensorEventProto.getId().isBlank()) {
            log.error("Идентификатор события не может быть пустым");
            throw new IllegalArgumentException("Идентификатор события датчика не может быть пустым");
        }
    }

    private SensorEventType mapPayloadCaseToEventType(SensorEventProto.PayloadCase payloadCase, String eventId) {
        if (payloadCase == SensorEventProto.PayloadCase.PAYLOAD_NOT_SET) {
            String errorMessage = String.format(
                    "Полезная нагрузка не установлена для события ID: %s", eventId);
            log.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        SensorEventType eventType = PAYLOAD_TO_EVENT_TYPE_MAP.get(payloadCase);

        if (eventType == null) {
            String errorMessage = String.format(
                    "Неподдерживаемый PayloadCase: %s для события ID: %s", payloadCase, eventId);
            log.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        log.debug("Успешно сопоставлен PayloadCase {} -> SensorEventType {} для события ID: {}",
                payloadCase, eventType, eventId);

        return eventType;
    }
}
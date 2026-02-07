package ru.yandex.practicum.gprc.sensor.mapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.dto.sensor.SensorEventDto;
import ru.yandex.practicum.entity.SensorEventType;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class GrpcToDtoMapper {
    private final SensorEventTypeMapper sensorEventTypeMapper;
    private final Map<SensorEventType, SensorEventMapper> sensorEventMappers;

    public GrpcToDtoMapper(SensorEventTypeMapper sensorEventTypeMapper, List<SensorEventMapper> sensorEventMappers) {
        this.sensorEventTypeMapper = sensorEventTypeMapper;
        this.sensorEventMappers = initializeSensorEventMappers(sensorEventMappers);
        log.info("GrpcToDtoMapper успешно инициализирован. Поддерживаемых типов событий: {}", sensorEventMappers.size());
    }

    public SensorEventDto fromProto(SensorEventProto sensorEventProto) {
        validateInput(sensorEventProto);
        SensorEventType eventType = sensorEventTypeMapper.fromProto(sensorEventProto);
        SensorEventMapper mapper = getMapperForEventType(eventType);
        SensorEventDto result = mapper.mapFromProto(sensorEventProto);
        validateMappingResult(result, sensorEventProto);
        log.debug("Преобразование успешно: тип {}, ID {}", eventType, sensorEventProto.getId());
        return result;
    }

    private Map<SensorEventType, SensorEventMapper> initializeSensorEventMappers(List<SensorEventMapper> sensorEventMappers) {
        return sensorEventMappers.stream().collect(Collectors.toMap(SensorEventMapper::getSupportedType, Function.identity(), (existing, replacement) -> {
            throw new IllegalStateException(String.format("Дублирующиеся мапперы для типа %s: %s и %s", existing.getSupportedType(), existing.getClass().getSimpleName(), replacement.getClass().getSimpleName()));
        }));
    }

    private void validateInput(SensorEventProto sensorEventProto) {
        if (sensorEventProto == null) {
            throw new IllegalArgumentException("SensorEventProto не может быть null");
        }
        if (sensorEventProto.getId() == null || sensorEventProto.getId().isBlank()) {
            throw new IllegalArgumentException("Идентификатор события не может быть пустым");
        }
        if (sensorEventProto.getHubId() == null || sensorEventProto.getHubId().isBlank()) {
            throw new IllegalArgumentException("Идентификатор хаба не может быть пустым");
        }
        if (sensorEventProto.getPayloadCase() == SensorEventProto.PayloadCase.PAYLOAD_NOT_SET) {
            throw new IllegalArgumentException("Тип события датчика не определен");
        }
    }

    private SensorEventMapper getMapperForEventType(SensorEventType eventType) {
        SensorEventMapper mapper = sensorEventMappers.get(eventType);
        if (mapper == null) {
            throw new IllegalArgumentException(String.format("Не найден маппер для типа события: %s", eventType));
        }
        return mapper;
    }

    private void validateMappingResult(SensorEventDto result, SensorEventProto sensorEventProto) {
        if (result == null) {
            throw new IllegalStateException("Маппер вернул null");
        }
        if (!result.getId().equals(sensorEventProto.getId())) {
            throw new IllegalStateException(String.format("ID события не совпадает после преобразования: %s != %s", sensorEventProto.getId(), result.getId()));
        }
        if (!result.getHubId().equals(sensorEventProto.getHubId())) {
            throw new IllegalStateException(String.format("Hub ID не совпадает после преобразования: %s != %s", sensorEventProto.getHubId(), result.getHubId()));
        }
        if (result.getType() == null) {
            throw new IllegalStateException("Тип события не может быть null после преобразования");
        }
    }
}
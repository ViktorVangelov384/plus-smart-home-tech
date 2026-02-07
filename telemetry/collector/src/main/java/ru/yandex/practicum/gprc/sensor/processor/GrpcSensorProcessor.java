package ru.yandex.practicum.gprc.sensor.processor;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.sensor.SensorEventDto;
import ru.yandex.practicum.gprc.sensor.mapper.GrpcToDtoMapper;
import ru.yandex.practicum.mapper.sensor.SensorEventHandler; // Ваш интерфейс
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GrpcSensorProcessor {

    private final GrpcToDtoMapper grpcToDtoMapper;
    private final List<SensorEventHandler> handlers;

    public void processSensorEvent(SensorEventProto sensorEventProto) {
        try {
            SensorEventDto sensorEventDto = grpcToDtoMapper.fromProto(sensorEventProto);

            SensorEventHandler handler = findHandlerForEvent(sensorEventDto);

            handler.handle(sensorEventDto);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(
                    String.format("Ошибка обработки события датчика ID: %s",
                            sensorEventProto != null ? sensorEventProto.getId() : "unknown"),
                    e
            );
        }
    }

    private SensorEventHandler findHandlerForEvent(SensorEventDto sensorEventDto) {
        return handlers.stream()
                .filter(handler -> handler.canHandle(sensorEventDto))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("Не найден обработчик для типа события: %s. ID: %s",
                                sensorEventDto.getType(),
                                sensorEventDto.getId())
                ));
    }
}

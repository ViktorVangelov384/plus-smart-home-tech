package ru.practicum.hub.producer;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.practicum.hub.config.SensorConfig;
import ru.practicum.hub.emulators.*;
import ru.practicum.hub.service.GrpcClientService;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventDataProducer {

    private static final String INITIALIZATION_LOG_TEMPLATE = "Добавлено {} {} датчиков";
    private static final String SENDING_LOG_TEMPLATE = "Отправлено событие: {}";
    private static final String ERROR_LOG_TEMPLATE = "Ошибка при создании/отправке события для эмулятора {}";

    private final SensorConfig sensorConfig;
    private final GrpcClientService grpcClientService;

    private final List<SensorEventEmulator> emulators = Collections.synchronizedList(new ArrayList<>());
    private volatile boolean emulatorsInitialized = false;
    private final Object initializationLock = new Object();

    @PostConstruct
    public void init() {
        initializeEmulators();
    }

    public void initializeEmulators() {
        if (emulatorsInitialized) {
            log.debug("Эмуляторы уже инициализированы");
            return;
        }

        synchronized (initializationLock) {
            if (emulatorsInitialized) {
                return;
            }

            log.info("Инициализация эмуляторов событий...");

            final Map<String, Integer> sensorCounts = initializeAllEmulators();
            final int totalSensors = sensorCounts.values().stream().mapToInt(Integer::intValue).sum();

            emulatorsInitialized = true;
            log.info("Создано {} эмуляторов (всего {} датчиков)", emulators.size(), totalSensors);
            logSensorStatistics(sensorCounts);
        }
    }

    private Map<String, Integer> initializeAllEmulators() {
        final Map<String, Integer> sensorCounts = new HashMap<>();

        sensorCounts.put("motion", initializeEmulatorCollection(
                sensorConfig.getMotionSensors(),
                MotionSensorEventEmulator::new,
                "motion"
        ));

        sensorCounts.put("switch", initializeEmulatorCollection(
                sensorConfig.getSwitchSensors(),
                SwitchSensorEventEmulator::new,
                "switch"
        ));

        sensorCounts.put("temperature", initializeEmulatorCollection(
                sensorConfig.getTemperatureSensors(),
                TemperatureSensorEventEmulator::new,
                "temperature"
        ));

        sensorCounts.put("light", initializeEmulatorCollection(
                sensorConfig.getLightSensors(),
                LightSensorEventEmulator::new,
                "light"
        ));

        sensorCounts.put("climate", initializeEmulatorCollection(
                sensorConfig.getClimateSensors(),
                ClimateSensorEventEmulator::new,
                "climate"
        ));

        return sensorCounts;
    }

    private <T> int initializeEmulatorCollection(
            Collection<T> configs,
            java.util.function.Function<T, SensorEventEmulator> emulatorCreator,
            String sensorType
    ) {
        if (configs == null || configs.isEmpty()) {
            log.debug("Конфигурация для {} датчиков отсутствует или пуста", sensorType);
            return 0;
        }

        final List<SensorEventEmulator> createdEmulators = configs.stream()
                .map(emulatorCreator)
                .collect(Collectors.toList());

        emulators.addAll(createdEmulators);

        log.debug(INITIALIZATION_LOG_TEMPLATE, createdEmulators.size(), sensorType);
        return createdEmulators.size();
    }

    private void logSensorStatistics(Map<String, Integer> sensorCounts) {
        if (log.isDebugEnabled()) {
            sensorCounts.forEach((type, count) ->
                    log.debug("{} датчиков: {}", type, count)
            );
        }
    }

    @Scheduled(fixedDelayString = "${event.producer.delay:5000}")
    public void generateAndSendEvents() {
        if (!emulatorsInitialized) {
            initializeEmulators();
        }

        if (emulators.isEmpty()) {
            log.warn("Нет эмуляторов для генерации событий");
            return;
        }

        log.debug("Генерация событий для {} датчиков...", emulators.size());

        final SendingResult result = sendEventsFromEmulators();

        logResultStatistics(result);
    }

    private SendingResult sendEventsFromEmulators() {
        final AtomicInteger successCount = new AtomicInteger();
        final AtomicInteger errorCount = new AtomicInteger();

        emulators.forEach(emulator -> processEmulator(emulator, successCount, errorCount));

        return new SendingResult(successCount.get(), errorCount.get());
    }

    private void processEmulator(
            SensorEventEmulator emulator,
            AtomicInteger successCount,
            AtomicInteger errorCount
    ) {
        try {
            final SensorEventProto event = emulator.emulateEvent();
            grpcClientService.sendEvent(event);
            successCount.incrementAndGet();
            log.trace(SENDING_LOG_TEMPLATE, event.getId());
        } catch (Exception e) {
            errorCount.incrementAndGet();
            log.error(ERROR_LOG_TEMPLATE, emulator.getClass().getSimpleName(), e);
            if (log.isDebugEnabled()) {
                log.debug("Детали ошибки:", e);
            }
        }
    }

    private void logResultStatistics(SendingResult result) {
        if (result.hasErrors()) {
            log.warn("Отправка событий завершена. Успешно: {}, с ошибками: {}",
                    result.successCount, result.errorCount);
        } else {
            log.debug("Отправка событий завершена. Успешно: {}", result.successCount);
        }
    }

    private static class SendingResult {
        private final int successCount;
        private final int errorCount;

        SendingResult(int successCount, int errorCount) {
            this.successCount = successCount;
            this.errorCount = errorCount;
        }

        boolean hasErrors() {
            return errorCount > 0;
        }
    }
}
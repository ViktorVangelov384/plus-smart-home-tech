package ru.yandex.practicum;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ConfigurableApplicationContext;
import ru.yandex.practicum.consumer.hub.HubProcessor;
import ru.yandex.practicum.consumer.snapshot.SnapshotProcessor;

@Slf4j
@SpringBootApplication
@ConfigurationPropertiesScan
public class AnalyzerApp {

    public static void main(String[] args) {
        log.info("Инициализация приложения анализа телеметрии");
        ConfigurableApplicationContext springContext =
                SpringApplication.run(AnalyzerApp.class, args);

        HubProcessor hubDataProcessor = springContext.getBean(HubProcessor.class);
        SnapshotProcessor snapshotDataProcessor = springContext.getBean(SnapshotProcessor.class);

        launchProcessingThreads(hubDataProcessor, snapshotDataProcessor);

        configureGracefulTermination(springContext, hubDataProcessor, snapshotDataProcessor);
    }

    private static void launchProcessingThreads(
            HubProcessor hubProcessor,
            SnapshotProcessor snapshotProcessor) {

        Thread hubEventsThread = new Thread(hubProcessor, "Hub-Data-Processor-Thread");
        hubEventsThread.start();
        log.info("Процессор событий хаба активирован в потоке '{}'",
                hubEventsThread.getName());

        Thread snapshotProcessingThread = new Thread(snapshotProcessor,
                "Sensor-Snapshot-Processor-Thread");
        snapshotProcessingThread.start();
        log.info("Процессор снимков состояний активирован в потоке '{}'",
                snapshotProcessingThread.getName());
    }

    private static void configureGracefulTermination(
            ConfigurableApplicationContext applicationContext,
            HubProcessor hubProcessor,
            SnapshotProcessor snapshotProcessor) {

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.warn("Обнаружен сигнал завершения работы. Начинаю процедуру остановки...");

            terminateDataProcessors(hubProcessor, snapshotProcessor);

            pauseForCleanup();

            closeApplicationContext(applicationContext);

            log.info("Приложение анализа телеметрии успешно остановлено");
        }));
    }

    private static void terminateDataProcessors(
            HubProcessor hubProcessor,
            SnapshotProcessor snapshotProcessor) {

        log.debug("Инициирую остановку процессоров данных...");

        hubProcessor.terminate();
        log.debug("Процессор событий хаба получил команду на остановку");

        snapshotProcessor.terminate();
        log.debug("Процессор снимков состояний получил команду на остановку");
    }

    private static void pauseForCleanup() {
        try {
            final int shutdownGracePeriodMillis = 2500;
            log.debug("Ожидание завершения текущих операций ({} мс)...",
                    shutdownGracePeriodMillis);
            Thread.sleep(shutdownGracePeriodMillis);
        } catch (InterruptedException interruption) {
            log.warn("Поток завершения был прерван");
            Thread.currentThread().interrupt();
        }
    }

    private static void closeApplicationContext(
            ConfigurableApplicationContext context) {
    }
}
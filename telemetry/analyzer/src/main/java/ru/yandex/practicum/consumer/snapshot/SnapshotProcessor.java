package ru.yandex.practicum.consumer.snapshot;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.consumer.processor.KafkaMessageProcessor;
import ru.yandex.practicum.consumer.processor.ProcessorProperties;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.service.ScenarioAssessmentService;

import java.util.Properties;

@Slf4j
@Component
public class SnapshotProcessor extends KafkaMessageProcessor<SensorsSnapshotAvro> {

    private final Properties snapshotConsumerConfig;
    private final ScenarioAssessmentService scenarioAssessment;

    public SnapshotProcessor(ProcessorProperties processorConfig,
                             @Qualifier("snapshotClientConfig") Properties consumerConfiguration,
                             ScenarioAssessmentService assessmentService) {
        super(processorConfig);
        this.snapshotConsumerConfig = consumerConfiguration;
        this.scenarioAssessment= assessmentService;
        log.info("Создан процессор снимков состояний датчиков");
    }

    @Override
    protected Consumer<String, SensorsSnapshotAvro> initializeConsumer() {
        log.debug("Создание Kafka потребителя для обработки снимков");
        return new KafkaConsumer<>(snapshotConsumerConfig);
    }

    @Override
    protected void handleMessage(SensorsSnapshotAvro sensorSnapshot) {
        String sourceHubId = sensorSnapshot.getHubId();
        int connectedDevicesCount = sensorSnapshot.getSensorsState().size();

        log.debug("Обработка снимка состояния. Хаб: {}, Устройств: {}",
                sourceHubId, connectedDevicesCount);

        try {
            scenarioAssessment.assessSensorData(sensorSnapshot);
            log.debug("Снимок успешно обработан. Хаб: {}", sourceHubId);
        } catch (Exception assessmentError) {
            handleEvaluationException(sourceHubId, assessmentError);
            throw assessmentError;
        }
    }

    @Override
    protected String getTargetTopic() {
        return "telemetry.snapshots.v1";
    }

    @Override
    protected String getProcessorId() {
        return "SnapshotProcessor";
    }

    private void handleEvaluationException(String hubId, Exception error) {
        log.error("Сбой при анализе снимка состояния. Хаб: {}, Причина: {}",
                hubId, error.getMessage(), error);
    }
}

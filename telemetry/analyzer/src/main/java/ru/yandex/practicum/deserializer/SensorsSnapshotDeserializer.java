package ru.yandex.practicum.deserializer;

import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.io.IOException;
import java.util.Map;

public class SensorsSnapshotDeserializer implements Deserializer<SensorsSnapshotAvro> {

    private static final Logger logger = LoggerFactory.getLogger(SensorsSnapshotDeserializer.class);
    private static final int UPPER_SIZE_BOUNDARY = 10 * 1024 * 1024;

    private final DatumReader<SensorsSnapshotAvro> dataReader;

    public SensorsSnapshotDeserializer() {
        this.dataReader = new SpecificDatumReader<>(SensorsSnapshotAvro.class);
    }

    @Override
    public void configure(final Map<String, ?> parameters, final boolean keyDeserializer) {
        logger.debug("Конфигурирование десериализатора снимков: ключ={}", keyDeserializer);
    }

    @Override
    public SensorsSnapshotAvro deserialize(final String kafkaTopic, final byte[] binaryContent) {
        if (binaryContent == null || binaryContent.length == 0) {
            logger.debug("Отсутствуют данные для обработки в топике {}", kafkaTopic);
            return null;
        }

        validateContentDimensions(kafkaTopic, binaryContent.length);

        try {
            final SensorsSnapshotAvro decodedSnapshot = decodeBinaryStream(binaryContent);
            logProcessingResult(kafkaTopic, decodedSnapshot);
            return decodedSnapshot;
        } catch (IOException decodingException) {
            handleDecodingFailure(kafkaTopic, binaryContent.length, decodingException);
            throw new SerializationException("Невозможно интерпретировать поток данных снимка", decodingException);
        }
    }

    @Override
    public void close() {
        logger.trace("Освобождение ресурсов десериализатора снимков");
    }

    private SensorsSnapshotAvro decodeBinaryStream(final byte[] dataStream) throws IOException {
        final Decoder binaryStreamDecoder = DecoderFactory.get().binaryDecoder(dataStream, null);
        return dataReader.read(null, binaryStreamDecoder);
    }

    private void validateContentDimensions(final String sourceTopic, final int contentLength) {
        if (contentLength > UPPER_SIZE_BOUNDARY) {
            final String sizeViolationMessage = String.format(
                    "Недопустимый объем данных в топике %s: фактически %d, допустимо не более %d байт",
                    sourceTopic, contentLength, UPPER_SIZE_BOUNDARY
            );
            logger.error(sizeViolationMessage);
            throw new SerializationException(sizeViolationMessage);
        }
    }

    private void logProcessingResult(final String originTopic, final SensorsSnapshotAvro processedSnapshot) {
        if (logger.isTraceEnabled()) {
            logger.trace(
                    "Данные успешно преобразованы. Источник: {}, Хаб: {}, Элементов: {}",
                    originTopic,
                    processedSnapshot.getHubId(),
                    processedSnapshot.getSensorsState() != null ?
                            processedSnapshot.getSensorsState().size() : 0
            );
        }
    }

    private void handleDecodingFailure(final String problematicTopic,
                                       final int failedContentLength,
                                       final IOException rootCause) {
        logger.error(
                "Прерывание обработки данных. Топик: {}, Объем: {} байт",
                problematicTopic,
                failedContentLength,
                rootCause
        );
    }
}

package ru.yandex.practicum.deserializer;

import org.apache.avro.Schema;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

import java.io.IOException;
import java.util.Map;

public class HubEventRouterDeserializer implements Deserializer<HubEventAvro> {

    private static final Logger logger = LoggerFactory.getLogger(HubEventRouterDeserializer.class);
    private final Schema eventSchema = HubEventAvro.getClassSchema();
    private static final int MAX_MESSAGE_CAPACITY = 10 * 1024 * 1024;

    @Override
    public void configure(final Map<String, ?> configuration, final boolean isKey) {
        logger.debug("Инициализация десериализатора событий хаба: ключ={}", isKey);
    }

    @Override
    public HubEventAvro deserialize(final String topicName, final byte[] rawData) {
        if (rawData == null) {
            logger.debug("Пропуск пустого сообщения из топика {}", topicName);
            return null;
        }

        if (rawData.length == 0) {
            logger.warn("Получено сообщение нулевой длины из топика {}", topicName);
            return null;
        }

        validateMessageSize(topicName, rawData.length);

        try {
            HubEventAvro parsedEvent = parseAvroMessage(rawData);
            logDeserializationSuccess(topicName, parsedEvent);
            return parsedEvent;
        } catch (IOException parsingError) {
            handleDeserializationError(topicName, rawData.length, parsingError);
            throw new SerializationException("Не удалось преобразовать данные события хаба", parsingError);
        }
    }

    @Override
    public void close() {
        logger.trace("Десериализатор событий хаба остановлен");
    }

    private HubEventAvro parseAvroMessage(byte[] messageBytes) throws IOException {
        BinaryDecoder dataDecoder = DecoderFactory.get().binaryDecoder(messageBytes, null);
        SpecificDatumReader<HubEventAvro> avroReader = new SpecificDatumReader<>(eventSchema);
        return avroReader.read(null, dataDecoder);
    }

    private void validateMessageSize(String topicName, int messageSize) {
        if (messageSize > MAX_MESSAGE_CAPACITY) {
            String errorDescription = String.format(
                    "Превышен допустимый размер сообщения из топика %s: %d > %d байт",
                    topicName, messageSize, MAX_MESSAGE_CAPACITY
            );
            logger.error(errorDescription);
            throw new SerializationException(errorDescription);
        }
    }

    private void logDeserializationSuccess(String topicName, HubEventAvro event) {
        if (logger.isTraceEnabled()) {
            logger.trace(
                    "Десериализация успешна. Топик: {}, Идентификатор хаба: {}, Время: {}",
                    topicName,
                    event.getHubId(),
                    event.getTimestamp()
            );
        }
    }

    private void handleDeserializationError(String topicName, int dataLength, IOException error) {
        logger.error(
                "Сбой десериализации сообщения. Источник: {}, Размер: {} байт",
                topicName,
                dataLength,
                error
        );
    }
}

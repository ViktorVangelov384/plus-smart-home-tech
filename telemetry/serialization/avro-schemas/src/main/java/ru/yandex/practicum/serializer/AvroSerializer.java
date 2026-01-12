package ru.yandex.practicum.serializer;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

@Slf4j
public class AvroSerializer implements Serializer<SpecificRecordBase> {

    private static final EncoderFactory ENCODER_FACTORY = EncoderFactory.get();

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        log.debug("AvroSerializer configured for key: {}", isKey);
    }

    @Override
    public byte[] serialize(String topic, SpecificRecordBase data) {
        if (data == null) {
            return null;
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] serializedData = serializeAvroRecord(data, outputStream);
            logSerializationInfo(topic, data, serializedData);
            return serializedData;

        } catch (IOException e) {
            log.error("Ошибка создания ByteArrayOutputStream", e);
            throw new IllegalStateException("Внутренняя ошибка сериализатора", e);
        }
    }

    private byte[] serializeAvroRecord(SpecificRecordBase data, ByteArrayOutputStream outputStream) {
        try {
            DatumWriter<SpecificRecordBase> writer = createDatumWriter(data);
            BinaryEncoder encoder = ENCODER_FACTORY.binaryEncoder(outputStream, null);

            writer.write(data, encoder);
            encoder.flush();

            return outputStream.toByteArray();

        } catch (IOException e) {
            String errorMessage = String.format(
                    "Ошибка сериализации Avro объекта %s",
                    data.getClass().getSimpleName()
            );
            log.error(errorMessage, e);
            throw new SerializationException(errorMessage, e);
        }
    }

    private DatumWriter<SpecificRecordBase> createDatumWriter(SpecificRecordBase data) {
        return new SpecificDatumWriter<>(data.getSchema());
    }

    private void logSerializationInfo(String topic, SpecificRecordBase data, byte[] serializedData) {
        if (log.isDebugEnabled()) {
            log.debug("Сериализовано Avro - Топик: {}, Тип: {}, Размер: {} байт",
                    topic,
                    data.getClass().getSimpleName(),
                    serializedData.length
            );
        }
    }

    @Override
    public void close() {
        log.debug("AvroSerializer closed");
    }
}


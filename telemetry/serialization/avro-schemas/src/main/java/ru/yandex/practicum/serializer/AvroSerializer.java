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

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        log.info("Настройка AvroSerializer");
    }

    @Override
    public byte[] serialize(String topic, SpecificRecordBase data) {
        if (data == null) {
            return null;
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            DatumWriter<SpecificRecordBase> writer = new SpecificDatumWriter<>(data.getSchema());
            BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(out, null);

            writer.write(data, encoder);
            encoder.flush();

            byte[] result = out.toByteArray();

            log.debug("Сериализовано Avro: {} байт для {}",
                    result.length, data.getClass().getSimpleName());

            return result;

        } catch (IOException e) {
            throw new SerializationException("Ошибка сериализации Avro", e);
        }
    }

    @Override
    public void close() {
    }
}
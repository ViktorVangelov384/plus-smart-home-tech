package ru.yandex.practicum.deserializer;

import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

import java.io.IOException;

public class SensorEventAvroDeserializer implements Deserializer<SensorEventAvro> {

    private final DecoderFactory decoderFactory = DecoderFactory.get();

    @Override
    public SensorEventAvro deserialize(String topic, byte[] data) {
        if (data == null) return null;

        try {
            BinaryDecoder decoder = decoderFactory.binaryDecoder(data, null);
            DatumReader<SensorEventAvro> reader =
                    new SpecificDatumReader<>(SensorEventAvro.getClassSchema());

            return reader.read(null, decoder);
        } catch (IOException e) {
            throw new SerializationException("Ошибка десериализации SensorEventAvro", e);
        }
    }
}
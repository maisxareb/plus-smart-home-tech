package ru.yandex.practicum.analyzer.serialization;

import lombok.SneakyThrows;
import org.apache.avro.Schema;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.kafka.common.serialization.Deserializer;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.io.ByteArrayInputStream;
import java.util.Map;

public class SensorsSnapshotDeserializer implements Deserializer<SensorsSnapshotAvro> {

    private final DecoderFactory decoderFactory;
    private final Schema schema;
    private DatumReader<SensorsSnapshotAvro> reader;

    public SensorsSnapshotDeserializer() {
        this.decoderFactory = DecoderFactory.get();
        this.schema = SensorsSnapshotAvro.getClassSchema();
        this.reader = new SpecificDatumReader<>(schema);
    }

    public SensorsSnapshotDeserializer(DecoderFactory decoderFactory, Schema schema) {
        this.decoderFactory = decoderFactory;
        this.schema = schema;
        this.reader = new SpecificDatumReader<>(schema);
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @SneakyThrows
    @Override
    public SensorsSnapshotAvro deserialize(String topic, byte[] data) {
        if (data == null) {
            return null;
        }

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data)) {
            Decoder decoder = decoderFactory.binaryDecoder(inputStream, null);
            return reader.read(null, decoder);
        }
    }

    @Override
    public void close() {
    }
}
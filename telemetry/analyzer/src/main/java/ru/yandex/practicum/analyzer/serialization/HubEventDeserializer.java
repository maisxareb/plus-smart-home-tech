package ru.yandex.practicum.analyzer.serialization;

import lombok.SneakyThrows;
import org.apache.avro.Schema;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.kafka.common.serialization.Deserializer;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

import java.io.ByteArrayInputStream;
import java.util.Map;

public class HubEventDeserializer implements Deserializer<HubEventAvro> {

    private final DecoderFactory decoderFactory;
    private final Schema schema;
    private DatumReader<HubEventAvro> reader;

    public HubEventDeserializer() {
        this.decoderFactory = DecoderFactory.get();
        this.schema = HubEventAvro.getClassSchema();
        this.reader = new SpecificDatumReader<>(schema);
    }

    public HubEventDeserializer(DecoderFactory decoderFactory, Schema schema) {
        this.decoderFactory = decoderFactory;
        this.schema = schema;
        this.reader = new SpecificDatumReader<>(schema);
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @SneakyThrows
    @Override
    public HubEventAvro deserialize(String topic, byte[] data) {
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
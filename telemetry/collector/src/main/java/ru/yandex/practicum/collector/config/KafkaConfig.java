package ru.yandex.practicum.collector.config;

import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    public static class AvroSerializer implements Serializer<Object> {
        @Override
        public byte[] serialize(String topic, Object data) {
            if (data == null) {
                return null;
            }

            try {
                if (data instanceof SpecificRecord) {
                    SpecificRecord avroRecord = (SpecificRecord) data;
                    return serializeSpecificRecord(avroRecord);
                } else {
                    throw new SerializationException(
                            "Объект должен быть экземпляром SpecificRecord. Фактический тип: " + data.getClass().getName()
                    );
                }
            } catch (Exception e) {
                throw new SerializationException("Ошибка сериализации Avro сообщения для топика: " + topic, e);
            }
        }

        private byte[] serializeSpecificRecord(SpecificRecord record) throws IOException {
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(out, null);
                DatumWriter<SpecificRecord> writer = new SpecificDatumWriter<>(record.getSchema());
                writer.write(record, encoder);
                encoder.flush();
                return out.toByteArray();
            }
        }
    }

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringSerializer");
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                AvroSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);

        configProps.put(ProducerConfig.CLIENT_ID_CONFIG, "collector-producer");
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        configProps.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "1");

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
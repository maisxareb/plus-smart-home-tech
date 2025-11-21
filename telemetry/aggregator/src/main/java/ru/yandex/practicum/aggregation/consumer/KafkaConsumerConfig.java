package ru.yandex.practicum.aggregation.consumer;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.key-deserializer}")
    private String keySerializer;

    @Value("${spring.kafka.consumer.sensor-event-value-deserializer}")
    private String sensorValueDeserializer;

    @Value("${spring.kafka.consumer.snapshot-value-deserializer}")
    private String snapshotValueDeserializer;

    @Value("${spring.kafka.consumer.sensor-event-group-id}")
    private String sensorGroup;

    @Value("${spring.kafka.consumer.snapshot-group-id}")
    private String snapshotGroup;

    @Bean
    public ConsumerFactory<String, SensorEventAvro> sensorEventConsumerFactory() throws ClassNotFoundException {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, Class.forName(keySerializer));
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, Class.forName(sensorValueDeserializer));
        props.put(ConsumerConfig.GROUP_ID_CONFIG, sensorGroup);

        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConsumerFactory<String, SensorsSnapshotAvro> sensorsSnapshotConsumerFactory() throws ClassNotFoundException {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, Class.forName(keySerializer));
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, Class.forName(snapshotValueDeserializer));
        props.put(ConsumerConfig.GROUP_ID_CONFIG, snapshotGroup);

        return new DefaultKafkaConsumerFactory<>(props);
    }
}

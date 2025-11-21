package ru.yandex.practicum.collector.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaEventProducer {

    protected final KafkaTemplate<String, SpecificRecordBase> producer;

    @Value("${spring.kafka.topics.hub-topic-name}")
    private String hubTopic;
    @Value("${spring.kafka.topics.sensor-topic-name}")
    private String sensorTopic;

    private void send(SpecificRecordBase event, String hubId, Instant timestamp, String topic) {
        ProducerRecord<String, SpecificRecordBase> record = new ProducerRecord<>(
                topic,
                null,
                timestamp.toEpochMilli(),
                hubId,
                event
        );

        producer.send(record);
        producer.flush();
    }

    public void sendHubEvent(HubEventAvro hubEvent) {
        send(hubEvent, hubEvent.getHubId(), hubEvent.getTimestamp(), hubTopic);
    }

    public void sendSensorEvent(SensorEventAvro sensorEvent) {
        send(sensorEvent, sensorEvent.getHubId(), sensorEvent.getTimestamp(), sensorTopic);
    }
}
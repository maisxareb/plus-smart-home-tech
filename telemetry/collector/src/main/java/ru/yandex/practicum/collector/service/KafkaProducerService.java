package ru.yandex.practicum.collector.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendSensorEvent(String key, Object event) {
        try {
            log.info("Sending sensor event to Kafka: {}", event);
            kafkaTemplate.send("telemetry.sensors.v1", key, event);
            log.info("Sensor event sent successfully");
        } catch (Exception e) {
            log.error("Failed to send sensor event to Kafka", e);
        }
    }

    public void sendHubEvent(String key, Object event) {
        try {
            log.info("Sending hub event to Kafka: {}", event);
            kafkaTemplate.send("telemetry.hubs.v1", key, event);
            log.info("Hub event sent successfully");
        } catch (Exception e) {
            log.error("Failed to send hub event to Kafka", e);
        }
    }
}
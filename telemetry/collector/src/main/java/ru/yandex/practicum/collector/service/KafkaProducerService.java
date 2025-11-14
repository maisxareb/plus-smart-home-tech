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
        kafkaTemplate.send("telemetry.sensors.v1", key, event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.debug("Sensor event sent successfully to partition {}", result.getRecordMetadata().partition());
                    } else {
                        log.error("Failed to send sensor event", ex);
                    }
                });
    }

    public void sendHubEvent(String key, Object event) {
        kafkaTemplate.send("telemetry.hubs.v1", key, event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.debug("Hub event sent successfully to partition {}", result.getRecordMetadata().partition());
                    } else {
                        log.error("Failed to send hub event", ex);
                    }
                });
    }
}
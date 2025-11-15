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
            log.info("Отправка события датчика в Kafka: {}", event);
            kafkaTemplate.send("telemetry.sensors.v1", key, event);
            log.info("Событие датчика успешно отправлено");
        } catch (Exception e) {
            log.error("Не удалось отправить событие датчика в Kafka", e);
        }
    }

    public void sendHubEvent(String key, Object event) {
        try {
            log.info("Отправка события хаба в Kafka: {}", event);
            kafkaTemplate.send("telemetry.hubs.v1", key, event);
            log.info("Событие хаба успешно отправлено");
        } catch (Exception e) {
            log.error("Не удалось отправить событие хаба в Kafka", e);
        }
    }
}
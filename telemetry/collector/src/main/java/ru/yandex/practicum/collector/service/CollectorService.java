package ru.yandex.practicum.collector.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.collector.kafka.KafkaEventProducer;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

@Service
@RequiredArgsConstructor
public class CollectorService {

    private final KafkaEventProducer kafkaEventProducer;

    public void sendSensorEvent(SensorEventAvro event) {
        kafkaEventProducer.sendSensorEvent(event);
    }

    public void sendHubEvent(HubEventAvro event) {
        kafkaEventProducer.sendHubEvent(event);
    }
}
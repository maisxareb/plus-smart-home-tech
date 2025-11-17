package ru.yandex.practicum.collector.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaHealthService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void checkKafkaConnection() {
        try {
            log.info("Проверка подключения к Kafka на localhost:9092...");

            var testAvroMessage = SensorEventAvro.newBuilder()
                    .setId("health-check-sensor")
                    .setHubId("health-check-hub")
                    .setTimestamp(System.currentTimeMillis())
                    .build();

            var future = kafkaTemplate.send("health-check-topic", "health-check-key", testAvroMessage);

            var result = future.get(5000, java.util.concurrent.TimeUnit.MILLISECONDS);

            log.info("Подключение к Kafka успешно. Сообщение отправлено в топик: {}, раздел: {}",
                    result.getRecordMetadata().topic(),
                    result.getRecordMetadata().partition());

        } catch (org.apache.kafka.common.errors.TimeoutException e) {
            log.error("Таймаут подключения к Kafka: Не удалось подключиться к брокеру Kafka в течение 5 секунд");
        } catch (org.apache.kafka.common.errors.SerializationException e) {
            log.error("Ошибка сериализации Avro: {}", e.getMessage());
            log.info("Это ожидаемо - AvroSerializer работает только с объектами SpecificRecord");
        } catch (Exception e) {
            log.error("Неожиданная ошибка при проверке здоровья Kafka: {}", e.getMessage());
        }
    }

    public void checkKafkaConnectionSimple() {
        try {
            log.info("Простая проверка подключения к Kafka...");

            var producer = kafkaTemplate.getProducerFactory().createProducer();
            try {
                var metadata = producer.partitionsFor("telemetry.sensors.v1");
                log.info("Подключение к Kafka успешно. Метаданные доступных топиков получены.");
                log.info("ID кластера: {}", metadata.get(0).topic()); // Просто для демонстрации
            } finally {
                producer.close();
            }

        } catch (Exception e) {
            log.error("Подключение к Kafka не удалось: {}", e.getMessage());
        }
    }
}
package ru.yandex.practicum.analyzer.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.service.SnapshotProcessingService;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

@Slf4j
@Component
@RequiredArgsConstructor
public class SnapshotProcessor {

    private final Properties snapshotConsumerProperties;
    private final SnapshotProcessingService snapshotProcessingService;

    private volatile boolean running = true;
    private KafkaConsumer<String, SensorsSnapshotAvro> consumer;

    public void start() {
        log.info("Запуск SnapshotProcessor...");

        consumer = new KafkaConsumer<>(snapshotConsumerProperties);
        consumer.subscribe(Collections.singletonList("telemetry.snapshots.v1"));

        log.info("Подписан на топик: telemetry.snapshots.v1");

        try {
            while (running) {
                ConsumerRecords<String, SensorsSnapshotAvro> records = consumer.poll(Duration.ofMillis(1000));

                for (ConsumerRecord<String, SensorsSnapshotAvro> record : records) {
                    try {
                        log.debug("Обработка снапшота: key={}, offset={}, partition={}",
                                record.key(), record.offset(), record.partition());

                        snapshotProcessingService.processSnapshot(record.value());

                    } catch (Exception e) {
                        log.error("Ошибка обработки снапшота: key={}, offset={}",
                                record.key(), record.offset(), e);
                    }
                }

                if (!records.isEmpty()) {
                    consumer.commitSync();
                    log.debug("Зафиксированы оффсеты для {} записей", records.count());
                }
            }
        } catch (WakeupException e) {
            log.info("Вызов wakeup для SnapshotProcessor");
        } catch (Exception e) {
            log.error("Неожиданная ошибка в SnapshotProcessor", e);
        } finally {
            shutdown();
        }
    }

    public void shutdown() {
        log.info("Остановка SnapshotProcessor...");
        running = false;
        if (consumer != null) {
            consumer.wakeup();
        }
    }
}
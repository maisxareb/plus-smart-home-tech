package ru.yandex.practicum.analyzer.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.service.HubEventProcessingService;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

@Slf4j
@Component
@RequiredArgsConstructor
public class HubEventProcessor implements Runnable {

    private final Properties hubEventConsumerProperties;
    private final HubEventProcessingService hubEventProcessingService;

    private volatile boolean running = true;
    private KafkaConsumer<String, HubEventAvro> consumer;

    @Override
    public void run() {
        log.info("Запуск HubEventProcessor в отдельном потоке...");

        consumer = new KafkaConsumer<>(hubEventConsumerProperties);
        consumer.subscribe(Collections.singletonList("telemetry.hubs.v1"));

        log.info("Подписан на топик: telemetry.hubs.v1");

        try {
            while (running) {
                ConsumerRecords<String, HubEventAvro> records = consumer.poll(Duration.ofMillis(1000));

                for (ConsumerRecord<String, HubEventAvro> record : records) {
                    try {
                        log.debug("Обработка события хаба: key={}, offset={}, partition={}",
                                record.key(), record.offset(), record.partition());

                        hubEventProcessingService.processHubEvent(record.value());

                    } catch (Exception e) {
                        log.error("Ошибка обработки события хаба: key={}, offset={}",
                                record.key(), record.offset(), e);
                    }
                }

                if (!records.isEmpty()) {
                    consumer.commitSync();
                    log.debug("Зафиксированы оффсеты для {} записей событий хаба", records.count());
                }
            }
        } catch (WakeupException e) {
            log.info("Вызов wakeup для HubEventProcessor");
        } catch (Exception e) {
            log.error("Неожиданная ошибка в HubEventProcessor", e);
        } finally {
            shutdown();
        }
    }

    public void shutdown() {
        log.info("Остановка HubEventProcessor...");
        running = false;
        if (consumer != null) {
            consumer.wakeup();
        }
    }
}
package ru.yandex.practicum.aggregation.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.aggregation.producer.SnapshotProducer;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregationStarter {

    private final ConsumerFactory<String, SensorEventAvro> eventConsumerFactory;
    private final ConsumerFactory<String, SensorsSnapshotAvro> snapshotConsumerFactory;
    private final SnapshotProducer snapshotProducer;
    private final CheckUpdateState checkUpdateState;

    @Value("${spring.kafka.topics.sensor-topic-name}")
    private String sensorsTopic;

    @Value("${spring.kafka.topics.snapshots-topic-name}")
    private String snapshotsTopic;

    private final List<SensorsSnapshotAvro> snapshotsList = new ArrayList<>();

    public void start() {
        log.info("Начало метода start()");
        try (
                Consumer<String, SensorsSnapshotAvro> snapshotConsumer = snapshotConsumerFactory.createConsumer();
                Consumer<String, SensorEventAvro> eventConsumer = eventConsumerFactory.createConsumer()
        ) {
            snapshotConsumer.subscribe(List.of(snapshotsTopic));
            eventConsumer.subscribe(List.of(sensorsTopic));

            while (true) {
                var snapshotRecords = snapshotConsumer.poll(Duration.ofMillis(100));
                snapshotRecords.forEach(record -> {
                    checkUpdateState.putSnapshot(record.value());
                    log.info("Снапшот hubId={} загружен из Kafka", record.value().getHubId());
                });
                snapshotConsumer.commitSync();

                var eventRecords = eventConsumer.poll(Duration.ofMillis(100));
                for (var record : eventRecords) {
                    SensorEventAvro event = record.value();
                    Optional<SensorsSnapshotAvro> updatedSnapshot =
                            checkUpdateState.updateState(event);
                    updatedSnapshot.ifPresent(snapshot -> {
                        try {
                            snapshotProducer.sendSnapshot(snapshot);
                        } catch (Exception e) {
                            log.error("Ошибка при отправке события в snapshots.v1: hubId={}",
                                    snapshot.getHubId(), e);
                        }
                    });
                }
                eventConsumer.commitSync();
            }
        } catch (WakeupException ignored) {
        } catch (Exception e) {
            log.error("Ошибка при агрегации событий от датчиков", e);
        } finally {
            try {
                snapshotProducer.flush();
            } catch (Exception e) {
                log.error("Ошибка закрытия продюсера", e);
            }
        }
    }
}
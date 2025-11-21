package ru.yandex.practicum.aggregation.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

@Service
@RequiredArgsConstructor
@Slf4j
public class SnapshotProducer {

    private final KafkaTemplate<String, SpecificRecordBase> snapshotKafkaTemplate;

    @Value("${spring.kafka.topics.snapshots-topic-name}")
    private String snapshotsTopic;

    public void sendSnapshot(SensorsSnapshotAvro snapshot) {
        snapshotKafkaTemplate.send(snapshotsTopic, snapshot.getHubId(), snapshot);
        log.info("Отправлен снапшот hubId={}", snapshot.getHubId());
    }

    public void flush() {
        snapshotKafkaTemplate.flush();
    }
}
package ru.yandex.practicum.collector.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.collector.model.HubEvent;
import ru.yandex.practicum.collector.model.SensorEvent;

@Slf4j
@Service
@RequiredArgsConstructor
public class CollectorService {

    private final AvroConversionService avroConversionService;
    private final KafkaProducerService kafkaProducerService;

    public void processSensorEvent(SensorEvent event) {
        try {
            var avroEvent = avroConversionService.convertToAvro(event);
            kafkaProducerService.sendSensorEvent(event.getHubId(), avroEvent);
            log.info("Processed sensor event for hub: {}, sensor: {}", event.getHubId(), event.getId());
        } catch (Exception e) {
            log.error("Error processing sensor event: {}", event, e);
            throw new RuntimeException("Failed to process sensor event", e);
        }
    }

    public void processHubEvent(HubEvent event) {
        try {
            var avroEvent = avroConversionService.convertToAvro(event);
            kafkaProducerService.sendHubEvent(event.getHubId(), avroEvent);
            log.info("Processed hub event for hub: {}", event.getHubId());
        } catch (Exception e) {
            log.error("Error processing hub event: {}", event, e);
            throw new RuntimeException("Failed to process hub event", e);
        }
    }
}
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
            log.info("Processing sensor event: {}", event);
            var avroEvent = avroConversionService.convertToAvro(event);
            kafkaProducerService.sendSensorEvent(event.getHubId(), avroEvent);
            log.info("Successfully processed sensor event for hub: {}, sensor: {}", event.getHubId(), event.getId());
        } catch (Exception e) {
            log.error("Error processing sensor event: {}", event, e);
            throw new RuntimeException("Failed to process sensor event: " + e.getMessage(), e);
        }
    }

    public void processHubEvent(HubEvent event) {
        try {
            log.info("Processing hub event: {}", event);
            var avroEvent = avroConversionService.convertToAvro(event);
            kafkaProducerService.sendHubEvent(event.getHubId(), avroEvent);
            log.info("Successfully processed hub event for hub: {}", event.getHubId());
        } catch (Exception e) {
            log.error("Error processing hub event: {}", event, e);
            throw new RuntimeException("Failed to process hub event: " + e.getMessage(), e);
        }
    }
}
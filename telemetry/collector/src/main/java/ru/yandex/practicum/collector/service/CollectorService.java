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
            log.info("Обработка события датчика: {}", event);
            var avroEvent = avroConversionService.convertToAvro(event);
            kafkaProducerService.sendSensorEvent(event.getHubId(), avroEvent);
            log.info("Успешно обработанное сенсорное событие для хаба: {}, датчик: {}", event.getHubId(), event.getId());
        } catch (Exception e) {
            log.error("Событие датчика обработки ошибок: {}", event, e);
            throw new RuntimeException("Ошибка обработки события датчика", e);
        }
    }

    public void processHubEvent(HubEvent event) {
        try {
            log.info("Событие хаба данных: {}", event);
            var avroEvent = avroConversionService.convertToAvro(event);
            kafkaProducerService.sendHubEvent(event.getHubId(), avroEvent);
            log.info("Событие хаба успешно обработано для хаба: {}", event.getHubId());
        } catch (Exception e) {
            log.error("Ошибка обработки события хаба: {}", event, e);
            throw new RuntimeException("Ошибка обработки события хаба", e);
        }
    }
}
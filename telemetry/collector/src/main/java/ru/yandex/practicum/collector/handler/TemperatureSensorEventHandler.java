package ru.yandex.practicum.collector.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;

@Slf4j
@Component
public class TemperatureSensorEventHandler implements SensorEventHandler {

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.TEMPERATURE_SENSOR;
    }

    @Override
    public void handle(SensorEventProto event) {
        var tempSensor = event.getTemperatureSensor();
        log.info("Обработка датчика температуры: {}°C ({}°F)",
                tempSensor.getTemperatureC(), tempSensor.getTemperatureF());

        if (tempSensor.getTemperatureC() > 30) {
            log.warn("Высокая температура! Возможно перегрев.");
        } else if (tempSensor.getTemperatureC() < 10) {
            log.warn("Низкая температура! Возможно замерзание.");
        }
    }
}
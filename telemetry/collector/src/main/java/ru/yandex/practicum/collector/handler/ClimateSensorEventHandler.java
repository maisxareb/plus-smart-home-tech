package ru.yandex.practicum.collector.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;

@Slf4j
@Component
public class ClimateSensorEventHandler implements SensorEventHandler {

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.CLIMATE_SENSOR;
    }

    @Override
    public void handle(SensorEventProto event) {
        var climateSensor = event.getClimateSensor();
        log.info("Обработка климатического датчика: температура={}°C, влажность={}%, CO2={}ppm",
                climateSensor.getTemperatureC(), climateSensor.getHumidity(), climateSensor.getCo2Level());

        if (climateSensor.getCo2Level() > 1000) {
            log.warn("Высокий уровень CO2! Необходимо проветривание.");
        }

        if (climateSensor.getHumidity() > 70) {
            log.warn("Высокая влажность! Возможна конденсация.");
        }
    }
}
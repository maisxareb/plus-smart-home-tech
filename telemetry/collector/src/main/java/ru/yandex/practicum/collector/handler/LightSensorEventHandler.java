package ru.yandex.practicum.collector.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;

@Slf4j
@Component
public class LightSensorEventHandler implements SensorEventHandler {

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.LIGHT_SENSOR;
    }

    @Override
    public void handle(SensorEventProto event) {
        var lightSensor = event.getLightSensor();
        log.info("Обработка датчика освещенности: освещенность={}, качество связи={}",
                lightSensor.getLuminosity(), lightSensor.getLinkQuality());

        if (lightSensor.getLuminosity() < 100) {
            log.info("Низкая освещенность - возможно ночное время");
        } else if (lightSensor.getLuminosity() > 800) {
            log.info("Высокая освещенность - возможно прямой солнечный свет");
        }
    }
}
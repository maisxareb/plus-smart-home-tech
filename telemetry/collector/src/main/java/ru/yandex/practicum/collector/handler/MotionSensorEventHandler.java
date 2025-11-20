package ru.yandex.practicum.collector.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;

@Slf4j
@Component
public class MotionSensorEventHandler implements SensorEventHandler {

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.MOTION_SENSOR;
    }

    @Override
    public void handle(SensorEventProto event) {
        var motionSensor = event.getMotionSensor();
        log.info("Обработка датчика движения: движение={}, качество связи={}, напряжение={}",
                motionSensor.getMotion(), motionSensor.getLinkQuality(), motionSensor.getVoltage());

        if (motionSensor.getMotion() && motionSensor.getLinkQuality() < 50) {
            log.warn("Низкое качество связи при обнаружении движения!");
        }
    }
}
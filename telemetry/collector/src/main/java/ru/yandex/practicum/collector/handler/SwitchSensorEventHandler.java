package ru.yandex.practicum.collector.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;

@Slf4j
@Component
public class SwitchSensorEventHandler implements SensorEventHandler {

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.SWITCH_SENSOR;
    }

    @Override
    public void handle(SensorEventProto event) {
        var switchSensor = event.getSwitchSensor();
        log.info("Обработка переключателя: состояние={}", switchSensor.getState());

        if (switchSensor.getState()) {
            log.info("Переключатель активирован");
        } else {
            log.info("Переключатель деактивирован");
        }
    }
}
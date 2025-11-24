package ru.yandex.practicum.collector.grpc;

import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

public interface SensorConverter {

    SensorEventAvro convertToClimateAvro(SensorEventProto proto);

    SensorEventAvro convertToTemperatureAvro(SensorEventProto proto);

    SensorEventAvro convertToLightAvro(SensorEventProto proto);

    SensorEventAvro convertToSwitchAvro(SensorEventProto proto);

    SensorEventAvro convertToMotionAvro(SensorEventProto proto);
}
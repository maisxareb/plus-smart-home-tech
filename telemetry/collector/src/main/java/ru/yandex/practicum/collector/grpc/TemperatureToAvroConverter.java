package ru.yandex.practicum.collector.grpc;

import com.google.protobuf.Timestamp;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.grpc.telemetry.event.TemperatureSensorProto;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.TemperatureSensorAvro;

import java.time.Instant;

@Component
public class TemperatureToAvroConverter {

    public SensorEventAvro convertToAvro(SensorEventProto proto) {
        Timestamp protoTimestamp = proto.getTimestamp();
        Instant instant = Instant.ofEpochSecond(protoTimestamp.getSeconds(), protoTimestamp.getNanos());

        TemperatureSensorAvro temperaturePayload = convert(proto.getTemperatureSensor());

        return SensorEventAvro.newBuilder()
                .setId(proto.getId())
                .setHubId(proto.getHubId())
                .setTimestamp(instant)
                .setPayload(temperaturePayload)
                .build();
    }

    private TemperatureSensorAvro convert(TemperatureSensorProto protoPayload) {
        return TemperatureSensorAvro.newBuilder()
                .setTemperatureC(protoPayload.getTemperatureC())
                .setTemperatureF(protoPayload.getTemperatureF())
                .build();
    }
}
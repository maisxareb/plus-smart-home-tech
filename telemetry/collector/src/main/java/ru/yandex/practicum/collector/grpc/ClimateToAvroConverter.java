package ru.yandex.practicum.collector.grpc;

import com.google.protobuf.Timestamp;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.ClimateSensorProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

import java.time.Instant;

@Component
public class ClimateToAvroConverter {

    public SensorEventAvro convertToAvro(SensorEventProto proto) {
        Timestamp protoTimestamp = proto.getTimestamp();
        Instant instant = Instant.ofEpochSecond(protoTimestamp.getSeconds(), protoTimestamp.getNanos());

        ClimateSensorAvro climatePayload = convert(proto.getClimateSensor());

        return SensorEventAvro.newBuilder()
                .setId(proto.getId())
                .setHubId(proto.getHubId())
                .setTimestamp(instant)
                .setPayload(climatePayload)
                .build();
    }

    private ClimateSensorAvro convert(ClimateSensorProto protoPayload) {
        return ClimateSensorAvro.newBuilder()
                .setTemperatureC(protoPayload.getTemperatureC())
                .setCo2Level(protoPayload.getCo2Level())
                .setHumidity(protoPayload.getHumidity())
                .build();
    }
}
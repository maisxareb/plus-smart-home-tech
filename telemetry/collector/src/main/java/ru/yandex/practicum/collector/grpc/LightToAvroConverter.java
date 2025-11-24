package ru.yandex.practicum.collector.grpc;

import com.google.protobuf.Timestamp;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.LightSensorProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

import java.time.Instant;

@Component
public class LightToAvroConverter {

    public SensorEventAvro convertToAvro(SensorEventProto proto) {
        Timestamp protoTimestamp = proto.getTimestamp();
        Instant instant = Instant.ofEpochSecond(protoTimestamp.getSeconds(), protoTimestamp.getNanos());

        LightSensorAvro lightPayload = convert(proto.getLightSensor());

        return SensorEventAvro.newBuilder()
                .setId(proto.getId())
                .setHubId(proto.getHubId())
                .setTimestamp(instant)
                .setPayload(lightPayload)
                .build();
    }

    private LightSensorAvro convert(LightSensorProto protoPayload) {
        return LightSensorAvro.newBuilder()
                .setLinkQuality(protoPayload.getLinkQuality())
                .setLuminosity(protoPayload.getLuminosity())
                .build();
    }
}
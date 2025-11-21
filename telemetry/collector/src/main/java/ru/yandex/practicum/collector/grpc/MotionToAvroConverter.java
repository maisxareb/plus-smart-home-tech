package ru.yandex.practicum.collector.grpc;

import com.google.protobuf.Timestamp;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.MotionSensorProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

import java.time.Instant;

@Component
public class MotionToAvroConverter {

    public SensorEventAvro convertToAvro(SensorEventProto proto) {
        Timestamp protoTimestamp = proto.getTimestamp();
        Instant instant = Instant.ofEpochSecond(protoTimestamp.getSeconds(), protoTimestamp.getNanos());

        MotionSensorAvro motionSensorAvro = convert(proto.getMotionSensor());

        return SensorEventAvro.newBuilder()
                .setId(proto.getId())
                .setHubId(proto.getHubId())
                .setTimestamp(instant)
                .setPayload(motionSensorAvro)
                .build();
    }

    private MotionSensorAvro convert(MotionSensorProto protoPayload) {
        return MotionSensorAvro.newBuilder()
                .setMotion(protoPayload.getMotion())
                .setLinkQuality(protoPayload.getLinkQuality())
                .setVoltage(protoPayload.getVoltage())
                .build();
    }

}
package ru.yandex.practicum.collector.grpc;

import com.google.protobuf.Timestamp;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.DeviceRemovedEventProto;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

import java.time.Instant;

@Component
public class DeviceRemoveToAvroConverter {

    public HubEventAvro convertToAvro(HubEventProto proto) {
        Timestamp protoTimestamp = proto.getTimestamp();
        Instant instant = Instant.ofEpochSecond(protoTimestamp.getSeconds(), protoTimestamp.getNanos());

        DeviceRemovedEventAvro deviceRemoveToAvroConverter = convert(proto.getDeviceRemoved());

        return HubEventAvro.newBuilder()
                .setHubId(proto.getHubId())
                .setTimestamp(instant)
                .setPayload(deviceRemoveToAvroConverter)
                .build();
    }

    private DeviceRemovedEventAvro convert(DeviceRemovedEventProto proto) {
        return DeviceRemovedEventAvro.newBuilder()
                .setId(proto.getId())
                .build();
    }
}
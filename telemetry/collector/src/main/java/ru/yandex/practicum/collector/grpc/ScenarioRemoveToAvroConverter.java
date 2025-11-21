package ru.yandex.practicum.collector.grpc;

import com.google.protobuf.Timestamp;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioRemovedEventProto;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioRemovedEventAvro;

import java.time.Instant;

@Component
public class ScenarioRemoveToAvroConverter {

    public HubEventAvro convertToAvro(HubEventProto proto) {
        Timestamp protoTimestamp = proto.getTimestamp();
        Instant instant = Instant.ofEpochSecond(protoTimestamp.getSeconds(), protoTimestamp.getNanos());

        ScenarioRemovedEventAvro scenarioRemovedEventAvro = convert(proto.getScenarioRemoved());

        return HubEventAvro.newBuilder()
                .setHubId(proto.getHubId())
                .setTimestamp(instant)
                .setPayload(scenarioRemovedEventAvro)
                .build();
    }

    private ScenarioRemovedEventAvro convert(ScenarioRemovedEventProto proto) {
        return ScenarioRemovedEventAvro.newBuilder()
                .setName(proto.getName())
                .build();
    }
}
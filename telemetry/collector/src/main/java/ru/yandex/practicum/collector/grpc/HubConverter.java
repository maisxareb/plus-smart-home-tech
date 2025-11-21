package ru.yandex.practicum.collector.grpc;

import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

public interface HubConverter {

    HubEventAvro convertToScenarioAdded(HubEventProto proto);

    HubEventAvro convertToScenarioRemove(HubEventProto proto);

    HubEventAvro convertToDeviceAdded(HubEventProto proto);

    HubEventAvro convertToDeviceRemove(HubEventProto proto);
}
package ru.yandex.practicum.collector.grpc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.collector.model.*;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.MotionSensorProto;
import ru.yandex.practicum.grpc.telemetry.event.TemperatureSensorProto;
import ru.yandex.practicum.grpc.telemetry.event.LightSensorProto;
import ru.yandex.practicum.grpc.telemetry.event.ClimateSensorProto;
import ru.yandex.practicum.grpc.telemetry.event.SwitchSensorProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceAddedEventProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceRemovedEventProto;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioAddedEventProto;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioRemovedEventProto;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioConditionProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;

import java.time.Instant;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ProtoToModelConverter {

    public ru.yandex.practicum.collector.model.SensorEvent convertToModel(SensorEventProto proto) {
        SensorEventProto.PayloadCase payloadCase = proto.getPayloadCase();

        switch (payloadCase) {
            case MOTION_SENSOR:
                return convertMotionSensor(proto);
            case TEMPERATURE_SENSOR:
                return convertTemperatureSensor(proto);
            case LIGHT_SENSOR:
                return convertLightSensor(proto);
            case CLIMATE_SENSOR:
                return convertClimateSensor(proto);
            case SWITCH_SENSOR:
                return convertSwitchSensor(proto);
            case PAYLOAD_NOT_SET:
            default:
                throw new IllegalArgumentException("Неизвестный тип события датчика: " + payloadCase);
        }
    }

    public ru.yandex.practicum.collector.model.HubEvent convertToModel(HubEventProto proto) {
        HubEventProto.PayloadCase payloadCase = proto.getPayloadCase();

        switch (payloadCase) {
            case DEVICE_ADDED:
                return convertDeviceAdded(proto);
            case DEVICE_REMOVED:
                return convertDeviceRemoved(proto);
            case SCENARIO_ADDED:
                return convertScenarioAdded(proto);
            case SCENARIO_REMOVED:
                return convertScenarioRemoved(proto);
            case PAYLOAD_NOT_SET:
            default:
                throw new IllegalArgumentException("Неизвестный тип события хаба: " + payloadCase);
        }
    }

    private MotionSensorEvent convertMotionSensor(SensorEventProto proto) {
        MotionSensorProto motionSensor = proto.getMotionSensor();
        MotionSensorEvent event = new MotionSensorEvent();
        setCommonSensorFields(event, proto);
        event.setLinkQuality(motionSensor.getLinkQuality());
        event.setMotion(motionSensor.getMotion());
        event.setVoltage(motionSensor.getVoltage());
        return event;
    }

    private TemperatureSensorEvent convertTemperatureSensor(SensorEventProto proto) {
        TemperatureSensorProto tempSensor = proto.getTemperatureSensor();
        TemperatureSensorEvent event = new TemperatureSensorEvent();
        setCommonSensorFields(event, proto);
        event.setTemperatureC(tempSensor.getTemperatureC());
        event.setTemperatureF(tempSensor.getTemperatureF());
        return event;
    }

    private LightSensorEvent convertLightSensor(SensorEventProto proto) {
        LightSensorProto lightSensor = proto.getLightSensor();
        LightSensorEvent event = new LightSensorEvent();
        setCommonSensorFields(event, proto);
        event.setLinkQuality(lightSensor.getLinkQuality());
        event.setLuminosity(lightSensor.getLuminosity());
        return event;
    }

    private ClimateSensorEvent convertClimateSensor(SensorEventProto proto) {
        ClimateSensorProto climateSensor = proto.getClimateSensor();
        ClimateSensorEvent event = new ClimateSensorEvent();
        setCommonSensorFields(event, proto);
        event.setTemperatureC(climateSensor.getTemperatureC());
        event.setHumidity(climateSensor.getHumidity());
        event.setCo2Level(climateSensor.getCo2Level());
        return event;
    }

    private SwitchSensorEvent convertSwitchSensor(SensorEventProto proto) {
        SwitchSensorProto switchSensor = proto.getSwitchSensor();
        SwitchSensorEvent event = new SwitchSensorEvent();
        setCommonSensorFields(event, proto);
        event.setState(switchSensor.getState());
        return event;
    }

    private DeviceAddedEvent convertDeviceAdded(HubEventProto proto) {
        DeviceAddedEventProto deviceAdded = proto.getDeviceAdded();
        DeviceAddedEvent event = new DeviceAddedEvent();
        setCommonHubFields(event, proto);
        event.setId(deviceAdded.getId());
        event.setDeviceType(DeviceType.valueOf(deviceAdded.getType().name()));
        return event;
    }

    private DeviceRemovedEvent convertDeviceRemoved(HubEventProto proto) {
        DeviceRemovedEventProto deviceRemoved = proto.getDeviceRemoved();
        DeviceRemovedEvent event = new DeviceRemovedEvent();
        setCommonHubFields(event, proto);
        event.setId(deviceRemoved.getId());
        return event;
    }

    private ScenarioAddedEvent convertScenarioAdded(HubEventProto proto) {
        ScenarioAddedEventProto scenarioAdded = proto.getScenarioAdded();
        ScenarioAddedEvent event = new ScenarioAddedEvent();
        setCommonHubFields(event, proto);
        event.setName(scenarioAdded.getName());
        event.setConditions(scenarioAdded.getConditionList().stream()
                .map(this::convertCondition)
                .collect(Collectors.toList()));
        event.setActions(scenarioAdded.getActionList().stream()
                .map(this::convertAction)
                .collect(Collectors.toList()));
        return event;
    }

    private ScenarioRemovedEvent convertScenarioRemoved(HubEventProto proto) {
        ScenarioRemovedEventProto scenarioRemoved = proto.getScenarioRemoved();
        ScenarioRemovedEvent event = new ScenarioRemovedEvent();
        setCommonHubFields(event, proto);
        event.setName(scenarioRemoved.getName());
        return event;
    }

    private ScenarioCondition convertCondition(ScenarioConditionProto proto) {
        ScenarioCondition condition = new ScenarioCondition();
        condition.setSensorId(proto.getSensorId());
        condition.setType(ConditionType.valueOf(proto.getType().name()));
        condition.setOperation(ConditionOperation.valueOf(proto.getOperation().name()));

        switch (proto.getValueCase()) {
            case BOOL_VALUE:
                condition.setValue(proto.getBoolValue() ? 1 : 0);
                break;
            case INT_VALUE:
                condition.setValue(proto.getIntValue());
                break;
            case VALUE_NOT_SET:
            default:
                condition.setValue(null);
        }

        return condition;
    }

    private DeviceAction convertAction(DeviceActionProto proto) {
        DeviceAction action = new DeviceAction();
        action.setSensorId(proto.getSensorId());
        action.setType(ActionType.valueOf(proto.getType().name()));
        action.setValue(proto.hasValue() ? proto.getValue() : null);
        return action;
    }

    private void setCommonSensorFields(SensorEvent event, SensorEventProto proto) {
        event.setId(proto.getId());
        event.setHubId(proto.getHubId());
        event.setTimestamp(Instant.ofEpochSecond(
                proto.getTimestamp().getSeconds(),
                proto.getTimestamp().getNanos()
        ));
    }

    private void setCommonHubFields(HubEvent event, HubEventProto proto) {
        event.setHubId(proto.getHubId());
        event.setTimestamp(Instant.ofEpochSecond(
                proto.getTimestamp().getSeconds(),
                proto.getTimestamp().getNanos()
        ));
    }
}
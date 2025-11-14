package ru.yandex.practicum.collector.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.collector.model.*;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.util.stream.Collectors;

@Service
public class AvroConversionService {

    public SensorEventAvro convertToAvro(SensorEvent event) {
        SensorEventAvro.Builder builder = SensorEventAvro.newBuilder()
                .setId(event.getId())
                .setHubId(event.getHubId())
                .setTimestamp(event.getTimestamp().toEpochMilli());

        switch (event.getType()) {
            case LIGHT_SENSOR_EVENT:
                LightSensorEvent lightEvent = (LightSensorEvent) event;
                builder.setPayload(LightSensorAvro.newBuilder()
                        .setLinkQuality(lightEvent.getLinkQuality())
                        .setLuminosity(lightEvent.getLuminosity())
                        .build());
                break;

            case TEMPERATURE_SENSOR_EVENT:
                TemperatureSensorEvent tempEvent = (TemperatureSensorEvent) event;
                builder.setPayload(TemperatureSensorAvro.newBuilder()
                        .setTemperatureC(tempEvent.getTemperatureC())
                        .setTemperatureF(tempEvent.getTemperatureF())
                        .build());
                break;

            case SWITCH_SENSOR_EVENT:
                SwitchSensorEvent switchEvent = (SwitchSensorEvent) event;
                builder.setPayload(SwitchSensorAvro.newBuilder()
                        .setState(switchEvent.getState())
                        .build());
                break;

            case CLIMATE_SENSOR_EVENT:
                ClimateSensorEvent climateEvent = (ClimateSensorEvent) event;
                builder.setPayload(ClimateSensorAvro.newBuilder()
                        .setTemperatureC(climateEvent.getTemperatureC())
                        .setHumidity(climateEvent.getHumidity())
                        .setCo2Level(climateEvent.getCo2Level())
                        .build());
                break;

            case MOTION_SENSOR_EVENT:
                MotionSensorEvent motionEvent = (MotionSensorEvent) event;
                builder.setPayload(MotionSensorAvro.newBuilder()
                        .setLinkQuality(motionEvent.getLinkQuality())
                        .setMotion(motionEvent.getMotion())
                        .setVoltage(motionEvent.getVoltage())
                        .build());
                break;
        }

        return builder.build();
    }

    public HubEventAvro convertToAvro(HubEvent event) {
        HubEventAvro.Builder builder = HubEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setTimestamp(event.getTimestamp().toEpochMilli());

        switch (event.getType()) {
            case DEVICE_ADDED:
                DeviceAddedEvent deviceAdded = (DeviceAddedEvent) event;
                builder.setPayload(DeviceAddedEventAvro.newBuilder()
                        .setId(deviceAdded.getId())
                        .setDeviceType(DeviceTypeAvro.valueOf(deviceAdded.getDeviceType().name()))
                        .build());
                break;

            case DEVICE_REMOVED:
                DeviceRemovedEvent deviceRemoved = (DeviceRemovedEvent) event;
                builder.setPayload(DeviceRemovedEventAvro.newBuilder()
                        .setId(deviceRemoved.getId())
                        .build());
                break;

            case SCENARIO_ADDED:
                ScenarioAddedEvent scenarioAdded = (ScenarioAddedEvent) event;
                builder.setPayload(ScenarioAddedEventAvro.newBuilder()
                        .setName(scenarioAdded.getName())
                        .setConditions(scenarioAdded.getConditions().stream()
                                .map(this::convertConditionToAvro)
                                .collect(Collectors.toList()))
                        .setActions(scenarioAdded.getActions().stream()
                                .map(this::convertActionToAvro)
                                .collect(Collectors.toList()))
                        .build());
                break;

            case SCENARIO_REMOVED:
                ScenarioRemovedEvent scenarioRemoved = (ScenarioRemovedEvent) event;
                builder.setPayload(ScenarioRemovedEventAvro.newBuilder()
                        .setName(scenarioRemoved.getName())
                        .build());
                break;
        }

        return builder.build();
    }

    private ScenarioConditionAvro convertConditionToAvro(ScenarioCondition condition) {
        return ScenarioConditionAvro.newBuilder()
                .setSensorId(condition.getSensorId())
                .setType(ConditionTypeAvro.valueOf(condition.getType().name()))
                .setOperation(ConditionOperationAvro.valueOf(condition.getOperation().name()))
                .setValue(condition.getValue())
                .build();
    }

    private DeviceActionAvro convertActionToAvro(DeviceAction action) {
        return DeviceActionAvro.newBuilder()
                .setSensorId(action.getSensorId())
                .setType(ActionTypeAvro.valueOf(action.getType().name()))
                .setValue(action.getValue())
                .build();
    }
}
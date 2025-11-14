package ru.yandex.practicum.collector.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.collector.model.*;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.util.stream.Collectors;

@Slf4j
@Service
public class AvroConversionService {

    public Object convertToAvro(SensorEvent event) {
        try {
            SensorEventAvro.Builder builder = SensorEventAvro.newBuilder()
                    .setId(event.getId())
                    .setHubId(event.getHubId())
                    .setTimestamp(event.getTimestamp().toEpochMilli());

            switch (event.getType()) {
                case LIGHT_SENSOR_EVENT:
                    LightSensorEvent lightEvent = (LightSensorEvent) event;
                    builder.setPayload(LightSensorAvro.newBuilder()
                            .setLinkQuality(lightEvent.getLinkQuality() != null ? lightEvent.getLinkQuality() : 0)
                            .setLuminosity(lightEvent.getLuminosity() != null ? lightEvent.getLuminosity() : 0)
                            .build());
                    break;

                case TEMPERATURE_SENSOR_EVENT:
                    TemperatureSensorEvent tempEvent = (TemperatureSensorEvent) event;
                    builder.setPayload(TemperatureSensorAvro.newBuilder()
                            .setTemperatureC(tempEvent.getTemperatureC() != null ? tempEvent.getTemperatureC() : 0)
                            .setTemperatureF(tempEvent.getTemperatureF() != null ? tempEvent.getTemperatureF() : 0)
                            .build());
                    break;

                case SWITCH_SENSOR_EVENT:
                    SwitchSensorEvent switchEvent = (SwitchSensorEvent) event;
                    builder.setPayload(SwitchSensorAvro.newBuilder()
                            .setState(switchEvent.getState() != null ? switchEvent.getState() : false)
                            .build());
                    break;

                case CLIMATE_SENSOR_EVENT:
                    ClimateSensorEvent climateEvent = (ClimateSensorEvent) event;
                    builder.setPayload(ClimateSensorAvro.newBuilder()
                            .setTemperatureC(climateEvent.getTemperatureC() != null ? climateEvent.getTemperatureC() : 0)
                            .setHumidity(climateEvent.getHumidity() != null ? climateEvent.getHumidity() : 0)
                            .setCo2Level(climateEvent.getCo2Level() != null ? climateEvent.getCo2Level() : 0)
                            .build());
                    break;

                case MOTION_SENSOR_EVENT:
                    MotionSensorEvent motionEvent = (MotionSensorEvent) event;
                    builder.setPayload(MotionSensorAvro.newBuilder()
                            .setLinkQuality(motionEvent.getLinkQuality() != null ? motionEvent.getLinkQuality() : 0)
                            .setMotion(motionEvent.getMotion() != null ? motionEvent.getMotion() : false)
                            .setVoltage(motionEvent.getVoltage() != null ? motionEvent.getVoltage() : 0)
                            .build());
                    break;
            }

            return builder.build();
        } catch (Exception e) {
            log.error("Error converting sensor event to Avro: {}", event, e);
            return SensorEventAvro.newBuilder()
                    .setId(event.getId())
                    .setHubId(event.getHubId())
                    .setTimestamp(event.getTimestamp().toEpochMilli())
                    .build();
        }
    }

    public Object convertToAvro(HubEvent event) {
        try {
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
        } catch (Exception e) {
            log.error("Error converting hub event to Avro: {}", event, e);
            return HubEventAvro.newBuilder()
                    .setHubId(event.getHubId())
                    .setTimestamp(event.getTimestamp().toEpochMilli())
                    .build();
        }
    }

    private ScenarioConditionAvro convertConditionToAvro(ScenarioCondition condition) {
        return ScenarioConditionAvro.newBuilder()
                .setSensorId(condition.getSensorId())
                .setType(ConditionTypeAvro.valueOf(condition.getType().name()))
                .setOperation(ConditionOperationAvro.valueOf(condition.getOperation().name()))
                .setValue(condition.getValue() != null ? condition.getValue() : 0)
                .build();
    }

    private DeviceActionAvro convertActionToAvro(DeviceAction action) {
        return DeviceActionAvro.newBuilder()
                .setSensorId(action.getSensorId())
                .setType(ActionTypeAvro.valueOf(action.getType().name()))
                .setValue(action.getValue() != null ? action.getValue() : 0)
                .build();
    }
}
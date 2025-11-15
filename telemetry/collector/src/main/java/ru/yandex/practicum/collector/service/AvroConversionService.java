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
                    LightSensorAvro lightPayload = LightSensorAvro.newBuilder()
                            .setLinkQuality(lightEvent.getLinkQuality() != null ? lightEvent.getLinkQuality() : 0)
                            .setLuminosity(lightEvent.getLuminosity() != null ? lightEvent.getLuminosity() : 0)
                            .build();
                    builder.setPayload(lightPayload);
                    break;

                case TEMPERATURE_SENSOR_EVENT:
                    TemperatureSensorEvent tempEvent = (TemperatureSensorEvent) event;
                    TemperatureSensorAvro tempPayload = TemperatureSensorAvro.newBuilder()
                            .setTemperatureC(tempEvent.getTemperatureC() != null ? tempEvent.getTemperatureC() : 0)
                            .setTemperatureF(tempEvent.getTemperatureF() != null ? tempEvent.getTemperatureF() : 0)
                            .build();
                    builder.setPayload(tempPayload);
                    break;

                case SWITCH_SENSOR_EVENT:
                    SwitchSensorEvent switchEvent = (SwitchSensorEvent) event;
                    SwitchSensorAvro switchPayload = SwitchSensorAvro.newBuilder()
                            .setState(switchEvent.getState() != null ? switchEvent.getState() : false)
                            .build();
                    builder.setPayload(switchPayload);
                    break;

                case CLIMATE_SENSOR_EVENT:
                    ClimateSensorEvent climateEvent = (ClimateSensorEvent) event;
                    ClimateSensorAvro climatePayload = ClimateSensorAvro.newBuilder()
                            .setTemperatureC(climateEvent.getTemperatureC() != null ? climateEvent.getTemperatureC() : 0)
                            .setHumidity(climateEvent.getHumidity() != null ? climateEvent.getHumidity() : 0)
                            .setCo2Level(climateEvent.getCo2Level() != null ? climateEvent.getCo2Level() : 0)
                            .build();
                    builder.setPayload(climatePayload);
                    break;

                case MOTION_SENSOR_EVENT:
                    MotionSensorEvent motionEvent = (MotionSensorEvent) event;
                    MotionSensorAvro motionPayload = MotionSensorAvro.newBuilder()
                            .setLinkQuality(motionEvent.getLinkQuality() != null ? motionEvent.getLinkQuality() : 0)
                            .setMotion(motionEvent.getMotion() != null ? motionEvent.getMotion() : false)
                            .setVoltage(motionEvent.getVoltage() != null ? motionEvent.getVoltage() : 0)
                            .build();
                    builder.setPayload(motionPayload);
                    break;
            }

            return builder.build();
        } catch (Exception e) {
            log.error("Ошибка при преобразовании события датчика в Avro: {}", event, e);
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
                    DeviceAddedEventAvro deviceAddedPayload = DeviceAddedEventAvro.newBuilder()
                            .setId(deviceAdded.getId())
                            .setDeviceType(DeviceTypeAvro.valueOf(deviceAdded.getDeviceType().name()))
                            .build();
                    builder.setPayload(deviceAddedPayload);
                    break;

                case DEVICE_REMOVED:
                    DeviceRemovedEvent deviceRemoved = (DeviceRemovedEvent) event;
                    DeviceRemovedEventAvro deviceRemovedPayload = DeviceRemovedEventAvro.newBuilder()
                            .setId(deviceRemoved.getId())
                            .build();
                    builder.setPayload(deviceRemovedPayload);
                    break;

                case SCENARIO_ADDED:
                    ScenarioAddedEvent scenarioAdded = (ScenarioAddedEvent) event;
                    ScenarioAddedEventAvro scenarioAddedPayload = ScenarioAddedEventAvro.newBuilder()
                            .setName(scenarioAdded.getName())
                            .setConditions(scenarioAdded.getConditions().stream()
                                    .map(this::convertConditionToAvro)
                                    .collect(Collectors.toList()))
                            .setActions(scenarioAdded.getActions().stream()
                                    .map(this::convertActionToAvro)
                                    .collect(Collectors.toList()))
                            .build();
                    builder.setPayload(scenarioAddedPayload);
                    break;

                case SCENARIO_REMOVED:
                    ScenarioRemovedEvent scenarioRemoved = (ScenarioRemovedEvent) event;
                    ScenarioRemovedEventAvro scenarioRemovedPayload = ScenarioRemovedEventAvro.newBuilder()
                            .setName(scenarioRemoved.getName())
                            .build();
                    builder.setPayload(scenarioRemovedPayload);
                    break;
            }

            return builder.build();
        } catch (Exception e) {
            log.error("Ошибка преобразования события хаба в Avro: {}", event, e);
            return HubEventAvro.newBuilder()
                    .setHubId(event.getHubId())
                    .setTimestamp(event.getTimestamp().toEpochMilli())
                    .build();
        }
    }

    private ScenarioConditionAvro convertConditionToAvro(ScenarioCondition condition) {
        ScenarioConditionAvro.Builder builder = ScenarioConditionAvro.newBuilder()
                .setSensorId(condition.getSensorId())
                .setType(ConditionTypeAvro.valueOf(condition.getType().name()))
                .setOperation(ConditionOperationAvro.valueOf(condition.getOperation().name()));

        if (condition.getValue() != null) {
            builder.setValue(condition.getValue());
        } else {
            builder.setValue(null);
        }

        return builder.build();
    }

    private DeviceActionAvro convertActionToAvro(DeviceAction action) {
        DeviceActionAvro.Builder builder = DeviceActionAvro.newBuilder()
                .setSensorId(action.getSensorId())
                .setType(ActionTypeAvro.valueOf(action.getType().name()));

        if (action.getValue() != null) {
            builder.setValue(action.getValue());
        } else {
            builder.setValue(null);
        }

        return builder.build();
    }
}
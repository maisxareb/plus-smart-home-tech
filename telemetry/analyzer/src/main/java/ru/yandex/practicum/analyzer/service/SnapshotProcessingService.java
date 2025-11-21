package ru.yandex.practicum.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.analyzer.entity.*;
import ru.yandex.practicum.analyzer.model.DeviceStateData;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SnapshotProcessingService {

    private final ScenarioService scenarioService;
    private final ActionExecutionService actionExecutionService;

    public void processSnapshot(SensorsSnapshotAvro snapshot) {
        String hubId = snapshot.getHubId();
        log.info("НАЧАЛО ОБРАБОТКИ СНАПШОТА ДЛЯ ХАБА: {}", hubId);

        List<Scenario> scenarios = scenarioService.getScenariosByHubId(hubId);
        log.info("Найдено сценариев для хаба {}: {}", hubId, scenarios.size());

        if (scenarios.isEmpty()) {
            log.info("Сценарии не найдены для хаба: {}", hubId);
            return;
        }

        Map<String, DeviceStateData> deviceStates = extractDeviceStates(snapshot);
        log.info("Извлечено состояний устройств: {}", deviceStates.size());
        deviceStates.forEach((deviceId, state) ->
                log.debug("Устройство {}: {}", deviceId, state)
        );

        for (Scenario scenario : scenarios) {
            log.info("Проверка сценария: '{}' для хаба: {}", scenario.getName(), hubId);

            if (scenario.getConditions() == null || scenario.getConditions().isEmpty()) {
                log.warn("Сценарий '{}' не имеет условий", scenario.getName());
                continue;
            }

            boolean conditionsMet = evaluateScenario(scenario, deviceStates);

            if (conditionsMet) {
                log.info("УСЛОВИЯ СЦЕНАРИЯ '{}' ВЫПОЛНЕНЫ для хаба: {}", scenario.getName(), hubId);
                executeScenarioActions(scenario, hubId);
            } else {
                log.info("Условия сценария '{}' НЕ выполнены для хаба: {}", scenario.getName(), hubId);
            }
        }

        log.info("ЗАВЕРШЕНИЕ ОБРАБОТКИ СНАПШОТА ДЛЯ ХАБА: {}", hubId);
    }

    private Map<String, DeviceStateData> extractDeviceStates(SensorsSnapshotAvro snapshot) {
        return snapshot.getSensorsState().stream()
                .collect(Collectors.toMap(
                        DeviceState::getDeviceId,
                        this::convertToDeviceStateData
                ));
    }

    private DeviceStateData convertToDeviceStateData(DeviceState deviceState) {
        return DeviceStateData.builder()
                .deviceId(deviceState.getDeviceId())
                .timestamp(deviceState.getState().getTimestamp())
                .sensorState(deviceState.getState())
                .build();
    }

    private boolean evaluateScenario(Scenario scenario, Map<String, DeviceStateData> deviceStates) {
        log.debug("Оценка условий сценария '{}'", scenario.getName());

        for (ScenarioCondition scenarioCondition : scenario.getConditions()) {
            if (scenarioCondition.getSensor() == null || scenarioCondition.getCondition() == null) {
                log.warn("Неполные данные условия сценария для сценария: {}", scenario.getName());
                return false;
            }

            String sensorId = scenarioCondition.getSensor().getId();
            Condition condition = scenarioCondition.getCondition();
            DeviceStateData deviceState = deviceStates.get(sensorId);

            if (deviceState == null) {
                log.debug("Данные устройства {} не найдены в снапшоте", sensorId);
                return false;
            }

            if (!evaluateCondition(condition, deviceState)) {
                log.debug("Условие не выполнено для сенсора: {} в сценарии: {}", sensorId, scenario.getName());
                return false;
            }
        }
        return true;
    }

    private boolean evaluateCondition(Condition condition, DeviceStateData deviceState) {
        Integer sensorValue = extractSensorValue(condition.getType(), deviceState);
        if (sensorValue == null) {
            log.debug("Невозможно извлечь значение сенсора для типа условия: {}", condition.getType());
            return false;
        }

        boolean result = switch (condition.getOperation()) {
            case EQUALS -> sensorValue.equals(condition.getValue());
            case GREATER_THAN -> sensorValue > condition.getValue();
            case LOWER_THAN -> sensorValue < condition.getValue();
        };

        log.debug("Оценка условия: {} {} {} = {} (значение: {})",
                condition.getType(), condition.getOperation(), condition.getValue(), result, sensorValue);

        return result;
    }

    private Integer extractSensorValue(ConditionType conditionType, DeviceStateData deviceState) {
        var sensorState = deviceState.getSensorState();
        var data = sensorState.getData();

        return switch (conditionType) {
            case TEMPERATURE -> {
                if (data instanceof ru.yandex.practicum.kafka.telemetry.event.TemperatureSensorAvro temp) {
                    yield temp.getTemperatureC();
                } else if (data instanceof ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro climate) {
                    yield climate.getTemperatureC();
                }
                yield null;
            }
            case HUMIDITY -> {
                if (data instanceof ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro climate) {
                    yield climate.getHumidity();
                }
                yield null;
            }
            case CO2LEVEL -> {
                if (data instanceof ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro climate) {
                    yield climate.getCo2Level();
                }
                yield null;
            }
            case LUMINOSITY -> {
                if (data instanceof ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro light) {
                    yield light.getLuminosity();
                }
                yield null;
            }
            case MOTION -> {
                if (data instanceof ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro motion) {
                    yield motion.getMotion() ? 1 : 0;
                }
                yield null;
            }
            case SWITCH -> {
                if (data instanceof ru.yandex.practicum.kafka.telemetry.event.SwitchSensorAvro switchSensor) {
                    yield switchSensor.getState() ? 1 : 0;
                }
                yield null;
            }
        };
    }

    private void executeScenarioActions(Scenario scenario, String hubId) {
        log.info("ВЫПОЛНЕНИЕ ДЕЙСТВИЙ СЦЕНАРИЯ '{}' для хаба: {}", scenario.getName(), hubId);

        if (scenario.getActions() == null || scenario.getActions().isEmpty()) {
            log.warn("Сценарий '{}' не имеет действий для выполнения", scenario.getName());
            return;
        }

        List<ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto> actions = new ArrayList<>();

        for (ScenarioAction scenarioAction : scenario.getActions()) {
            if (scenarioAction.getAction() == null || scenarioAction.getSensor() == null) {
                log.warn("Неполные данные действия сценария для сценария: {}", scenario.getName());
                continue;
            }

            var action = scenarioAction.getAction();
            var sensor = scenarioAction.getSensor();

            log.info("Добавление действия: {} для устройства: {}", action.getType(), sensor.getId());

            var actionProto = ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto.newBuilder()
                    .setSensorId(sensor.getId())
                    .setType(ru.yandex.practicum.grpc.telemetry.event.ActionTypeProto.valueOf(action.getType().name()))
                    .setValue(action.getValue() != null ? action.getValue() : 0)
                    .build();

            actions.add(actionProto);
        }

        if (!actions.isEmpty()) {
            log.info("Отправка {} действий в Hub Router для сценария: {}", actions.size(), scenario.getName());
            actionExecutionService.executeActions(hubId, scenario.getName(), actions);
        } else {
            log.warn("Нет действительных действий для выполнения в сценарии: {}", scenario.getName());
        }
    }
}
package ru.yandex.practicum.analyzer;

import com.google.protobuf.util.Timestamps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.exception.EntityNotFoundException;
import ru.yandex.practicum.analyzer.model.Scenario;
import ru.yandex.practicum.analyzer.model.ScenarioAction;
import ru.yandex.practicum.analyzer.model.ScenarioCondition;
import ru.yandex.practicum.analyzer.repository.ScenarioActionRepository;
import ru.yandex.practicum.analyzer.repository.ScenarioConditionRepository;
import ru.yandex.practicum.analyzer.repository.ScenarioRepository;
import ru.yandex.practicum.analyzer.repository.SensorRepository;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.ActionTypeProto;
import ru.yandex.practicum.grpc.telemetry.hubrouter.DeviceActionRequest;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class CheckScenarios {

    private final ScenarioRepository scenarioRepository;
    private final ScenarioConditionRepository scenarioConditionRepository;
    private final ScenarioActionRepository scenarioActionRepository;
    private final SensorRepository sensorRepository;

    public List<DeviceActionRequest> checkScenarios(SensorsSnapshotAvro snapshot) {
        log.info("Начинаю проверку сценариев...");

        List<DeviceActionRequest> result = new ArrayList<>();

        List<Scenario> scenarioList = scenarioRepository.findByHubId(snapshot.getHubId());
        if (scenarioList.isEmpty()) {
            return result;
        }

        List<Long> scenarioIds = scenarioList.stream()
                .map(Scenario::getId)
                .toList();

        List<ScenarioCondition> allConditions =
                scenarioConditionRepository.findAllByScenarioIdIn(scenarioIds);
        List<ScenarioAction> allActions =
                scenarioActionRepository.findAllByScenarioIdIn(scenarioIds);

        Map<Long, List<ScenarioCondition>> conditionsByScenario = allConditions.stream()
                .collect(Collectors.groupingBy(sc -> sc.getScenario().getId()));
        Map<Long, List<ScenarioAction>> actionsByScenario = allActions.stream()
                .collect(Collectors.groupingBy(sa -> sa.getScenario().getId()));

        for (Scenario scenario : scenarioList) {
            List<ScenarioCondition> scenarioConditions =
                    conditionsByScenario.getOrDefault(scenario.getId(), List.of());

            boolean allConditionsTrue = scenarioConditions.stream()
                    .allMatch(condition -> checkCondition(condition, snapshot, snapshot.getHubId()));

            if (allConditionsTrue) {
                log.info("Все условия прошли проверку!");
                List<ScenarioAction> actions =
                        actionsByScenario.getOrDefault(scenario.getId(), List.of());

                for (ScenarioAction action : actions) {
                    // ИСПРАВЛЕНИЕ: преобразуем String в ActionTypeProto
                    String actionTypeString = action.getAction().getType();
                    ActionTypeProto actionTypeProto;
                    try {
                        actionTypeProto = ActionTypeProto.valueOf(actionTypeString);
                    } catch (IllegalArgumentException e) {
                        log.error("Неизвестный тип действия: {}", actionTypeString);
                        continue;
                    }

                    Integer actionValue = action.getAction().getValue();

                    DeviceActionProto.Builder deviceActionBuilder = DeviceActionProto.newBuilder()
                            .setSensorId(action.getSensor().getId())
                            .setType(actionTypeProto);

                    if (actionValue != null) {
                        deviceActionBuilder.setValue(actionValue);
                    }

                    DeviceActionProto deviceActionProto = deviceActionBuilder.build();

                    DeviceActionRequest request = DeviceActionRequest.newBuilder()
                            .setHubId(snapshot.getHubId())
                            .setScenarioName(action.getScenario().getName())
                            .setAction(deviceActionProto)
                            .setTimestamp(Timestamps.fromMillis(System.currentTimeMillis()))
                            .build();

                    result.add(request);
                }
            }
        }

        return result;
    }

    private boolean checkCondition(ScenarioCondition condition, SensorsSnapshotAvro snapshot, String hubId) {
        String sensorId = condition.getSensor().getId();

        SensorStateAvro state = null;
        for (DeviceState deviceState : snapshot.getSensorsState()) {
            if (sensorId.equals(deviceState.getDeviceId())) {
                state = deviceState.getState();
                break;
            }
        }

        if (state == null || state.getData() == null) {
            log.info("Данных для сенсора {} пока нет, пропускаем проверку", sensorId);
            return true;
        }

        sensorRepository.findByIdAndHubId(sensorId, hubId)
                .orElseThrow(() -> new EntityNotFoundException("Датчик " + sensorId + " не найден"));

        String operationString = condition.getCondition().getOperation();
        String typeString = condition.getCondition().getType();

        ConditionOperationAvro operation;
        ConditionTypeAvro deviceType;

        try {
            operation = ConditionOperationAvro.valueOf(operationString);
            deviceType = ConditionTypeAvro.valueOf(typeString);
        } catch (IllegalArgumentException e) {
            log.error("Неизвестный тип операции или условия: operation={}, type={}", operationString, typeString);
            return false;
        }

        Integer expectedValue = condition.getCondition().getValue();

        return switch (deviceType) {
            case MOTION -> {
                MotionSensorAvro data = (MotionSensorAvro) state.getData();
                int actual = data.getMotion() ? 1 : 0;
                yield checkOperation(operation, actual, expectedValue);
            }
            case LUMINOSITY -> {
                LightSensorAvro data = (LightSensorAvro) state.getData();
                int actual = data.getLuminosity();
                yield checkOperation(operation, actual, expectedValue);
            }
            case TEMPERATURE -> {
                ClimateSensorAvro data = (ClimateSensorAvro) state.getData();
                int actual = data.getTemperatureC();
                yield checkOperation(operation, actual, expectedValue);
            }
            case HUMIDITY -> {
                ClimateSensorAvro data = (ClimateSensorAvro) state.getData();
                int actual = data.getHumidity();
                yield checkOperation(operation, actual, expectedValue);
            }
            case CO2LEVEL -> {
                ClimateSensorAvro data = (ClimateSensorAvro) state.getData();
                int actual = data.getCo2Level();
                yield checkOperation(operation, actual, expectedValue);
            }
            case SWITCH -> {
                SwitchSensorAvro data = (SwitchSensorAvro) state.getData();
                int actual = data.getState() ? 1 : 0;
                yield checkOperation(operation, actual, expectedValue);
            }
        };
    }

    private boolean checkOperation(ConditionOperationAvro operationType, Integer actual, Integer expected) {
        if (actual == null || expected == null) {
            return false;
        }

        return switch (operationType) {
            case EQUALS -> actual.equals(expected);
            case GREATER_THAN -> actual > expected;
            case LOWER_THAN -> actual < expected;
        };
    }
}
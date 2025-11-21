package ru.yandex.practicum.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.analyzer.entity.*;
import ru.yandex.practicum.analyzer.repository.ActionRepository;
import ru.yandex.practicum.analyzer.repository.ConditionRepository;
import ru.yandex.practicum.analyzer.repository.ScenarioRepository;
import ru.yandex.practicum.analyzer.repository.SensorRepository;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScenarioSetupService {

    private final ScenarioRepository scenarioRepository;
    private final SensorRepository sensorRepository;
    private final ConditionRepository conditionRepository;
    private final ActionRepository actionRepository;

    @Transactional
    public void addConditionToScenario(String hubId, String scenarioName, String sensorId,
                                       ConditionType conditionType, ConditionOperation operation, Integer value) {
        Optional<Scenario> scenarioOpt = scenarioRepository.findByHubIdAndName(hubId, scenarioName);
        Optional<Sensor> sensorOpt = sensorRepository.findByIdAndHubId(sensorId, hubId);

        if (scenarioOpt.isEmpty() || sensorOpt.isEmpty()) {
            log.error("Сценарий или сенсор не найден. Hub: {}, Scenario: {}, Sensor: {}",
                    hubId, scenarioName, sensorId);
            return;
        }

        Scenario scenario = scenarioOpt.get();
        Sensor sensor = sensorOpt.get();

        Condition condition = new Condition();
        condition.setType(conditionType);
        condition.setOperation(operation);
        condition.setValue(value);
        condition = conditionRepository.save(condition);

        ScenarioCondition scenarioCondition = new ScenarioCondition();
        scenarioCondition.setScenario(scenario);
        scenarioCondition.setSensor(sensor);
        scenarioCondition.setCondition(condition);

        scenario.getConditions().add(scenarioCondition);
        scenarioRepository.save(scenario);

        log.info("Добавлено условие к сценарию '{}': {} {} {} для сенсора {}",
                scenarioName, conditionType, operation, value, sensorId);
    }

    @Transactional
    public void addActionToScenario(String hubId, String scenarioName, String sensorId,
                                    ActionType actionType, Integer value) {
        Optional<Scenario> scenarioOpt = scenarioRepository.findByHubIdAndName(hubId, scenarioName);
        Optional<Sensor> sensorOpt = sensorRepository.findByIdAndHubId(sensorId, hubId);

        if (scenarioOpt.isEmpty() || sensorOpt.isEmpty()) {
            log.error("Сценарий или сенсор не найден. Hub: {}, Scenario: {}, Sensor: {}",
                    hubId, scenarioName, sensorId);
            return;
        }

        Scenario scenario = scenarioOpt.get();
        Sensor sensor = sensorOpt.get();

        // Создаем действие
        Action action = new Action();
        action.setType(actionType);
        action.setValue(value);
        action = actionRepository.save(action);

        // Создаем связь сценария с действием
        ScenarioAction scenarioAction = new ScenarioAction();
        scenarioAction.setScenario(scenario);
        scenarioAction.setSensor(sensor);
        scenarioAction.setAction(action);

        scenario.getActions().add(scenarioAction);
        scenarioRepository.save(scenario);

        log.info("Добавлено действие к сценарию '{}': {} значение {} для сенсора {}",
                scenarioName, actionType, value, sensorId);
    }

    @Transactional
    public void removeScenarioConditions(String hubId, String scenarioName) {
        Optional<Scenario> scenarioOpt = scenarioRepository.findByHubIdAndName(hubId, scenarioName);
        scenarioOpt.ifPresent(scenario -> {
            scenario.getConditions().clear();
            scenarioRepository.save(scenario);
            log.info("Удалены все условия сценария '{}'", scenarioName);
        });
    }

    @Transactional
    public void removeScenarioActions(String hubId, String scenarioName) {
        Optional<Scenario> scenarioOpt = scenarioRepository.findByHubIdAndName(hubId, scenarioName);
        scenarioOpt.ifPresent(scenario -> {
            scenario.getActions().clear();
            scenarioRepository.save(scenario);
            log.info("Удалены все действия сценария '{}'", scenarioName);
        });
    }
}
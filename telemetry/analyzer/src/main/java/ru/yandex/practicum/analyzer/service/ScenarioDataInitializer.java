package ru.yandex.practicum.analyzer.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.analyzer.entity.*;
import ru.yandex.practicum.analyzer.repository.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScenarioDataInitializer {

    private final ScenarioRepository scenarioRepository;
    private final SensorRepository sensorRepository;
    private final ConditionRepository conditionRepository;
    private final ActionRepository actionRepository;

    @PostConstruct
    @Transactional
    public void init() {
        log.info("Инициализация тестовых сценариев...");

        try {
            initHub1Scenarios();
            initHub2Scenarios();
            log.info("Тестовые сценарии успешно инициализированы");
        } catch (Exception e) {
            log.error("Ошибка инициализации тестовых сценариев", e);
        }
    }

    private void initHub1Scenarios() {
        Scenario autoLight = createScenario("hub-1", "Автосвет (коридор)");

        Condition motionCondition = createCondition(ConditionType.MOTION, ConditionOperation.EQUALS, 1);
        Condition lightCondition = createCondition(ConditionType.LUMINOSITY, ConditionOperation.LOWER_THAN, 50);

        Action lightAction = createAction(ActionType.ACTIVATE, 1);

        linkConditionToScenario(autoLight, "c7b8d4a1-8e37-4c1d-9130-5b0150e13954", motionCondition);
        linkConditionToScenario(autoLight, "ed9e9587-4148-4fb5-81e0-61d072568628", lightCondition);
        linkActionToScenario(autoLight, "006b61ad-cac8-4adf-9dce-43892b5d060f", lightAction);

        Scenario tempControl = createScenario("hub-1", "Регулировка температуры (спальня)");

        Condition tempCondition = createCondition(ConditionType.TEMPERATURE, ConditionOperation.LOWER_THAN, 18);
        Action heatAction = createAction(ActionType.ACTIVATE, 1);

        linkConditionToScenario(tempControl, "2b0bb4c1-7cf2-475a-a17c-e5cb6239d6e5", tempCondition);
        linkActionToScenario(tempControl, "0b9e1641-1a9f-4c43-9b24-6c3f0ccb000e", heatAction);
    }

    private void initHub2Scenarios() {
        Scenario turnOffLights = createScenario("hub-2", "Выключить весь свет");

        Condition switchCondition = createCondition(ConditionType.SWITCH, ConditionOperation.EQUALS, 1);
        Action turnOffAction = createAction(ActionType.DEACTIVATE, 0);

        linkConditionToScenario(turnOffLights, "14276dbc-d980-4dcc-853e-845ec38c5154", switchCondition);
        linkActionToScenario(turnOffLights, "14276dbc-d980-4dcc-853e-845ec38c5154", turnOffAction);
        linkActionToScenario(turnOffLights, "b2ec7f40-9c46-4b0b-8ee1-12d0bebde5c1", turnOffAction);
        linkActionToScenario(turnOffLights, "90811ee6-accb-401b-8442-8ec945bdaf29", turnOffAction);
        linkActionToScenario(turnOffLights, "f94d84a9-c9dd-41df-bbdc-70a8e609437b", turnOffAction);
    }

    private Scenario createScenario(String hubId, String name) {
        return scenarioRepository.findByHubIdAndName(hubId, name)
                .orElseGet(() -> {
                    Scenario scenario = new Scenario();
                    scenario.setHubId(hubId);
                    scenario.setName(name);
                    return scenarioRepository.save(scenario);
                });
    }

    private Condition createCondition(ConditionType type, ConditionOperation operation, Integer value) {
        Condition condition = new Condition();
        condition.setType(type);
        condition.setOperation(operation);
        condition.setValue(value);
        return conditionRepository.save(condition);
    }

    private Action createAction(ActionType type, Integer value) {
        Action action = new Action();
        action.setType(type);
        action.setValue(value);
        return actionRepository.save(action);
    }

    private void linkConditionToScenario(Scenario scenario, String sensorId, Condition condition) {
    }

    private void linkActionToScenario(Scenario scenario, String sensorId, Action action) {
    }
}
package ru.yandex.practicum.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.analyzer.entity.*;
import ru.yandex.practicum.analyzer.repository.ScenarioRepository;
import ru.yandex.practicum.analyzer.repository.SensorRepository;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScenarioService {

    private final ScenarioRepository scenarioRepository;
    private final SensorRepository sensorRepository;

    public List<Scenario> getScenariosByHubId(String hubId) {
        List<Scenario> scenariosWithConditions = scenarioRepository.findByHubIdWithConditions(hubId);

        if (scenariosWithConditions.isEmpty()) {
            return scenariosWithConditions;
        }

        List<Scenario> scenariosWithActions = scenarioRepository.findByHubIdWithActions(hubId);

        Map<Long, Scenario> scenarioMap = scenariosWithConditions.stream()
                .collect(Collectors.toMap(Scenario::getId, Function.identity()));

        for (Scenario scenarioWithActions : scenariosWithActions) {
            Scenario targetScenario = scenarioMap.get(scenarioWithActions.getId());
            if (targetScenario != null && scenarioWithActions.getActions() != null) {
                targetScenario.setActions(scenarioWithActions.getActions());
            }
        }

        return new ArrayList<>(scenarioMap.values());
    }

    @Transactional
    public void addScenario(Scenario scenario) {
        scenarioRepository.save(scenario);
        log.info("Added scenario: {} for hub: {}", scenario.getName(), scenario.getHubId());
    }

    @Transactional
    public void removeScenario(String hubId, String scenarioName) {
        Optional<Scenario> scenario = scenarioRepository.findByHubIdAndName(hubId, scenarioName);
        scenario.ifPresent(scenarioRepository::delete);
        log.info("Removed scenario: {} for hub: {}", scenarioName, hubId);
    }

    @Transactional
    public void addDeviceToHub(String hubId, String deviceId, DeviceType deviceType) {
        Sensor sensor = new Sensor();
        sensor.setId(deviceId);
        sensor.setHubId(hubId);
        sensor.setDeviceType(deviceType);
        sensorRepository.save(sensor);
        log.info("Added device: {} of type: {} to hub: {}", deviceId, deviceType, hubId);
    }

    @Transactional
    public void removeDeviceFromHub(String hubId, String deviceId) {
        sensorRepository.deleteByHubIdAndId(hubId, deviceId);
        log.info("Removed device: {} from hub: {}", deviceId, hubId);
    }
}
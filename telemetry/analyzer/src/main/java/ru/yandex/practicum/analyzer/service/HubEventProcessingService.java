package ru.yandex.practicum.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.analyzer.entity.*;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

@Slf4j
@Service
@RequiredArgsConstructor
public class HubEventProcessingService {

    private final ScenarioService scenarioService;

    public void processHubEvent(HubEventAvro hubEvent) {
        String hubId = hubEvent.getHubId();
        var payload = hubEvent.getPayload();

        try {
            switch (payload) {
                case ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro deviceAdded ->
                        handleDeviceAdded(hubId, deviceAdded);
                case ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro deviceRemoved ->
                        handleDeviceRemoved(hubId, deviceRemoved);
                case ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro scenarioAdded ->
                        handleScenarioAdded(hubId, scenarioAdded);
                case ru.yandex.practicum.kafka.telemetry.event.ScenarioRemovedEventAvro scenarioRemoved ->
                        handleScenarioRemoved(hubId, scenarioRemoved);
                default -> log.warn("Неизвестный тип события хаба для хаба: {}", hubId);
            }
        } catch (Exception e) {
            log.error("Ошибка обработки события хаба для хаба: {}", hubId, e);
        }
    }

    private void handleDeviceAdded(String hubId, ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro event) {
        DeviceType deviceType = DeviceType.valueOf(event.getDeviceType().name());
        scenarioService.addDeviceToHub(hubId, event.getId(), deviceType);
    }

    private void handleDeviceRemoved(String hubId, ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro event) {
        scenarioService.removeDeviceFromHub(hubId, event.getId());
    }

    private void handleScenarioAdded(String hubId, ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro event) {
        Scenario scenario = new Scenario();
        scenario.setHubId(hubId);
        scenario.setName(event.getName());

        scenarioService.addScenario(scenario);
    }

    private void handleScenarioRemoved(String hubId, ru.yandex.practicum.kafka.telemetry.event.ScenarioRemovedEventAvro event) {
        scenarioService.removeScenario(hubId, event.getName());
    }
}
package ru.yandex.practicum.analyzer.service;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc;
import ru.yandex.practicum.grpc.telemetry.hubrouter.DeviceActionRequest;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActionExecutionService {

    private final HubRouterControllerGrpc.HubRouterControllerBlockingStub hubRouterStub;

    public void executeActions(String hubId, String scenarioName, List<DeviceActionProto> actions) {
        log.info("Начало выполнения {} действий для сценария '{}' хаба {}",
                actions.size(), scenarioName, hubId);

        for (DeviceActionProto action : actions) {
            try {
                log.info("Отправка действия для устройства: {}, тип: {}, значение: {}",
                        action.getSensorId(), action.getType(), action.getValue());

                var request = DeviceActionRequest.newBuilder()
                        .setHubId(hubId)
                        .setScenarioName(scenarioName)
                        .setAction(action)
                        .build();

                Empty response = hubRouterStub.handleDeviceAction(request);
                log.info("Успешно выполнено действие для устройства: {} в сценарии: {} для хаба: {}",
                        action.getSensorId(), scenarioName, hubId);

            } catch (StatusRuntimeException e) {
                if (e.getStatus().getCode() == Status.Code.UNAVAILABLE) {
                    log.error("Hub Router недоступен для хаба: {}", hubId);
                } else {
                    log.error("Не удалось выполнить действие для устройства: {} в сценарии: {} для хаба: {}. Ошибка: {}",
                            action.getSensorId(), scenarioName, hubId, e.getStatus().getDescription());
                }
            } catch (Exception e) {
                log.error("Неожиданная ошибка выполнения действия для устройства: {} в сценарии: {} для хаба: {}",
                        action.getSensorId(), scenarioName, hubId, e);
            }
        }

        log.info("Завершение выполнения действий для сценария '{}'", scenarioName);
    }
}
package ru.yandex.practicum.collector.grpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.yandex.practicum.collector.service.CollectorService;
import ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerOuterClass.CollectResponse;
import ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerGrpc;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class EventController extends CollectorControllerGrpc.CollectorControllerImplBase {

    private final CollectorService collectorService;
    private final ProtoToModelConverter protoToModelConverter;

    @Override
    public void collectSensorEvent(SensorEventProto request, StreamObserver<CollectResponse> responseObserver) {
        try {
            log.info("Получено gRPC событие датчика типа: {}", request.getPayloadCase());

            var sensorEvent = protoToModelConverter.convertToModel(request);
            collectorService.processSensorEvent(sensorEvent);

            var response = CollectResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Событие датчика успешно обработано")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("Ошибка обработки gRPC события датчика: {}", request, e);
            var response = CollectResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Ошибка обработки: " + e.getMessage())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void collectHubEvent(HubEventProto request, StreamObserver<CollectResponse> responseObserver) {
        try {
            log.info("Получено gRPC событие хаба типа: {}", request.getPayloadCase());

            var hubEvent = protoToModelConverter.convertToModel(request);
            collectorService.processHubEvent(hubEvent);

            var response = CollectResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Событие хаба успешно обработано")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("Ошибка обработки gRPC события хаба: {}", request, e);
            var response = CollectResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Ошибка обработки: " + e.getMessage())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
}
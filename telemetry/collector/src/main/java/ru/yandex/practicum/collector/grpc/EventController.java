package ru.yandex.practicum.collector.grpc;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.yandex.practicum.collector.service.CollectorService;
import ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerGrpc;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class EventController extends CollectorControllerGrpc.CollectorControllerImplBase {

    private final SensorProtoToAvroConverter protoToAvroConverter;
    private final HubProtoToAvroConverter hubProtoToAvroConverter;
    private final CollectorService collectorService;

    @Override
    public void collectSensorEvent(SensorEventProto request, StreamObserver<Empty> responseObserver) {
        try {
            collectorService.sendSensorEvent(getSensorAvroObject(request));
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }
    }

    @Override
    public void collectHubEvent(HubEventProto request, StreamObserver<Empty> responseObserver) {
        try {
            log.info(request.toString());
            collectorService.sendHubEvent(getHubAvroObject(request));
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }
    }

    private SensorEventAvro getSensorAvroObject(SensorEventProto request) {
        return switch (request.getPayloadCase()) {
            case MOTION_SENSOR -> protoToAvroConverter.convertToMotionAvro(request);
            case TEMPERATURE_SENSOR -> protoToAvroConverter.convertToTemperatureAvro(request);
            case LIGHT_SENSOR -> protoToAvroConverter.convertToLightAvro(request);
            case CLIMATE_SENSOR -> protoToAvroConverter.convertToClimateAvro(request);
            case SWITCH_SENSOR -> protoToAvroConverter.convertToSwitchAvro(request);
            default -> throw new IllegalArgumentException("Unknown payload type: " + request.getPayloadCase());
        };
    }

    private HubEventAvro getHubAvroObject(HubEventProto request) {
        return switch (request.getPayloadCase()) {
            case DEVICE_ADDED -> hubProtoToAvroConverter.convertToDeviceAdded(request);
            case DEVICE_REMOVED -> hubProtoToAvroConverter.convertToDeviceRemove(request);
            case SCENARIO_ADDED -> hubProtoToAvroConverter.convertToScenarioAdded(request);
            case SCENARIO_REMOVED -> hubProtoToAvroConverter.convertToScenarioRemove(request);
            default -> throw new IllegalArgumentException("Unknown payload type: " + request.getPayloadCase());
        };
    }
}
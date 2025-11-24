package ru.yandex.practicum.collector.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class DeviceAction {
    private String sensorId;
    private ActionType type;
    private Integer value = null;
}
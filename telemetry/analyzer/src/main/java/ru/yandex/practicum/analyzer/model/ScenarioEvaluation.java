package ru.yandex.practicum.analyzer.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import ru.yandex.practicum.analyzer.entity.Scenario;

import java.util.List;

@Getter
@Builder
@ToString
public class ScenarioEvaluation {
    private Scenario scenario;
    private boolean conditionsMet;
    private List<String> failedConditions;
}
package ru.yandex.practicum.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.analyzer.entity.Scenario;

import java.util.List;
import java.util.Optional;

public interface ScenarioRepository extends JpaRepository<Scenario, Long> {
    List<Scenario> findByHubId(String hubId);
    Optional<Scenario> findByHubIdAndName(String hubId, String name);

    @Query("SELECT DISTINCT s FROM Scenario s " +
            "LEFT JOIN FETCH s.conditions sc " +
            "LEFT JOIN FETCH sc.sensor " +
            "LEFT JOIN FETCH sc.condition " +
            "WHERE s.hubId = :hubId")
    List<Scenario> findByHubIdWithConditions(@Param("hubId") String hubId);

    @Query("SELECT DISTINCT s FROM Scenario s " +
            "LEFT JOIN FETCH s.actions sa " +
            "LEFT JOIN FETCH sa.sensor " +
            "LEFT JOIN FETCH sa.action " +
            "WHERE s.hubId = :hubId")
    List<Scenario> findByHubIdWithActions(@Param("hubId") String hubId);

    @Query("SELECT DISTINCT s FROM Scenario s " +
            "LEFT JOIN FETCH s.conditions " +
            "WHERE s.hubId = :hubId")
    List<Scenario> findByHubIdWithConditionsOnly(@Param("hubId") String hubId);

    @Query("SELECT DISTINCT s FROM Scenario s " +
            "LEFT JOIN FETCH s.actions " +
            "WHERE s.hubId = :hubId")
    List<Scenario> findByHubIdWithActionsOnly(@Param("hubId") String hubId);

    boolean existsByHubIdAndName(String hubId, String name);
}
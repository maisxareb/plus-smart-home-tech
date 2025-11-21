package ru.yandex.practicum.analyzer.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan("ru.yandex.practicum.analyzer.entity")
@EnableJpaRepositories("ru.yandex.practicum.analyzer.repository")
public class DatabaseConfig {
}
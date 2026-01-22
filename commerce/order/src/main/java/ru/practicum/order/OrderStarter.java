package ru.practicum.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "ru.practicum.interaction.api")
public class OrderStarter {
    public static void main(String[] args) {
        SpringApplication.run(OrderStarter.class, args);
    }
}
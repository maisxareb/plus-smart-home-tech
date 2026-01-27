package ru.practicum.delivery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "ru.practicum.interaction.api")
public class DeliveryStarter {
    public static void main(String[] args) {
        SpringApplication.run(DeliveryStarter.class, args);
    }
}
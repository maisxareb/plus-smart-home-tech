package ru.practicum.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "ru.practicum.interaction.api")
public class PaymentStarter {
    public static void main(String[] args) {
        SpringApplication.run(PaymentStarter.class, args);
    }
}
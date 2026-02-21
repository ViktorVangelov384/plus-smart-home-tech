package ru.yandex.practicum.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GrpcEchoServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(GrpcEchoServiceApplication.class, args);

    }
}
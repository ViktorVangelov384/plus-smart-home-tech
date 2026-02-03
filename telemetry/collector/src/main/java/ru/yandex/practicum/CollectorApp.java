package ru.yandex.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import ru.yandex.practicum.config.ConfigKafka;

@SpringBootApplication
@EnableConfigurationProperties(ConfigKafka.class)
@EnableScheduling
public class CollectorApp {
    public static void main(String[] args) {
        SpringApplication.run(CollectorApp.class, args);
    }
}

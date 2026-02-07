package ru.yandex.practicum;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ConfigurableApplicationContext;
import ru.yandex.practicum.starter.AggregatorStarter;

@Slf4j
@SpringBootApplication
@ConfigurationPropertiesScan
public class AggregatorApp {
    public static void main(String[] args) {
        log.info("Запуск сервиса Aggregator");

        ConfigurableApplicationContext ctx = SpringApplication.run(AggregatorApp.class, args);

        AggregatorStarter starter = ctx.getBean(AggregatorStarter.class);
        starter.run();

    }
}
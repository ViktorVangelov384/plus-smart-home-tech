package ru.yandex.practicum.consumer.hub.handler;

public interface EventHandler {

    boolean canHandle(Object payload);

    void handle(String hubId, Object payload);
}

package ru.yandex.practicum.exception;

public class DuplicateDeliveryException extends RuntimeException {
    public DuplicateDeliveryException(String message) {
        super(message);
    }
}

package ru.yandex.practicum.exception;

public class NoProductInWarehouseException extends RuntimeException {
    public NoProductInWarehouseException(String message) {
        super(message);
    }
}

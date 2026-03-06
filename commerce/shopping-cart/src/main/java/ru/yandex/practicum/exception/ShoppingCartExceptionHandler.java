package ru.yandex.practicum.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ShoppingCartExceptionHandler {

    @ExceptionHandler(CartNotFoundException.class)
    public ResponseEntity<String> handleCartNotFound(CartNotFoundException e) {
        log.error("Корзина не найдена: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler(NoProductsInShoppingCartException.class)
    public ResponseEntity<String> handleNoProductsInCart(NoProductsInShoppingCartException e) {
        log.error("Товар не найден в корзине: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException e) {
        log.error("Неверный аргумент: {}", e.getMessage());

        HttpStatus status = HttpStatus.BAD_REQUEST;

        return ResponseEntity.status(status).body(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneric(Exception e) {
        log.error("Внутренняя ошибка сервера: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Внутренняя ошибка сервера");
    }
}

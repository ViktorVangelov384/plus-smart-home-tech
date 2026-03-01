package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.exception.NoProductInWarehouseException;
import ru.yandex.practicum.exception.ProductAlreadyExistsException;
import ru.yandex.practicum.model.warehouse.*;
import ru.yandex.practicum.service.WarehouseService;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/warehouse")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    @PutMapping
    public ResponseEntity<Void> addNewProduct(
            @Valid @RequestBody RegisterProductInWarehouseRequest request) {

        try {
            warehouseService.addNewProduct(request);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (IllegalArgumentException e) {
            log.error("Ошибка валидации: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (ProductAlreadyExistsException e) {
            log.error("Товар уже существует: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PostMapping("/check")
    public ResponseEntity<BookedProductsDto> checkAndBookProducts(
            @Valid @RequestBody ShoppingCartDto shoppingCart) {
        try {
            BookedProductsDto result = warehouseService.checkAndBookProducts(shoppingCart);
            return ResponseEntity.ok(result);
        } catch (NoProductInWarehouseException e) {
            log.error("Товар не найден на складе: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (IllegalArgumentException e) {
            log.error("Ошибка валидации корзины: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/add")
    public ResponseEntity<Void> addProductToWarehouse(
            @Valid @RequestBody RestockProductRequest request) {
        try {
            warehouseService.addProduct(request.getProductId(), request.getQuantity());
            return ResponseEntity.ok().build();
        } catch (NoProductInWarehouseException e) {
            log.error("Товар не найден на складе: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException e) {
            log.error("Ошибка валидации: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Внутренняя ошибка сервера: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();  // 500
        }
    }

    @GetMapping("/address")
    public ResponseEntity<AddressDto> getWarehouseAddress() {
        try {
            AddressDto address = warehouseService.getWarehouseAddress();
            return ResponseEntity.ok(address);
        } catch (Exception e) {
            log.error("Ошибка при получении адреса: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/products/{productId}/availability")
    public ResponseEntity<ProductStockResponse> checkStock(
            @PathVariable UUID productId,
            @RequestParam(required = false, defaultValue = "1") Long quantity) {
        try {
            return ResponseEntity.ok(new ProductStockResponse(productId, quantity, true));
        } catch (Exception e) {
            log.error("Ошибка при проверке наличия: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
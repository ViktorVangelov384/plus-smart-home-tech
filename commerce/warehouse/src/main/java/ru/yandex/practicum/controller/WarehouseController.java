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

        warehouseService.addNewProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/check")
    public ResponseEntity<BookedProductsDto> checkAndBookProducts(
            @Valid @RequestBody ShoppingCartDto shoppingCart) {

        BookedProductsDto result = warehouseService.checkAndBookProducts(shoppingCart);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/add")
    public ResponseEntity<Void> addProductToWarehouse(
            @Valid @RequestBody RestockProductRequest request) {

        warehouseService.addProduct(request.getProductId(), request.getQuantity());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/address")
    public ResponseEntity<AddressDto> getWarehouseAddress() {

        AddressDto address = warehouseService.getWarehouseAddress();
        return ResponseEntity.ok(address);
    }

    @GetMapping("/products/{productId}/availability")
    public ResponseEntity<ProductStockResponse> checkStock(
            @PathVariable UUID productId,
            @RequestParam(required = false, defaultValue = "1") Long quantity) {

        return ResponseEntity.ok(new ProductStockResponse(productId, quantity, true));
    }
}
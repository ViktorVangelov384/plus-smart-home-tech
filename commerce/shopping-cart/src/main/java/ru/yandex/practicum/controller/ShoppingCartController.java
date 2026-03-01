package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.model.warehouse.ShoppingCartDto;
import ru.yandex.practicum.service.ShoppingCartService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/shopping-cart")
public class ShoppingCartController {

    private final ShoppingCartService shoppingCartService;

    public ShoppingCartController(ShoppingCartService shoppingCartService) {
        this.shoppingCartService = shoppingCartService;
    }


    @GetMapping("/{username}")
    public ResponseEntity<ShoppingCartDto> getCartByUsername(@PathVariable String username) {

        ShoppingCartDto cart = shoppingCartService.getCartByUsername(username);

        return ResponseEntity.ok(cart);
    }

    @PostMapping("/{username}/products")
    public ResponseEntity<ShoppingCartDto> addProductsToCart(
            @PathVariable String username,
            @Valid @RequestBody Map<UUID, Long> products) {

        ShoppingCartDto updatedCart = shoppingCartService.addProductsToCart(username, products);

        return ResponseEntity.status(HttpStatus.CREATED).body(updatedCart);
    }

    @PutMapping("/{username}/products/{productId}")
    public ResponseEntity<ShoppingCartDto> updateProductQuantity(
            @PathVariable String username,
            @PathVariable UUID productId,
            @RequestParam @Valid @Min(1) Long quantity) {

        ShoppingCartDto updatedCart = shoppingCartService.updateProductQuantity(username, productId, quantity);

        return ResponseEntity.ok(updatedCart);
    }

    @DeleteMapping("/{username}/products")
    public ResponseEntity<ShoppingCartDto> removeProductsFromCart(
            @PathVariable String username,
            @Valid @RequestBody List<UUID> productIds) {

        ShoppingCartDto updatedCart = shoppingCartService.removeProductsFromCart(username, productIds);

        return ResponseEntity.ok(updatedCart);
    }

    @DeleteMapping("/{username}/clear")
    public ResponseEntity<ShoppingCartDto> clearCart(@PathVariable String username) {

        ShoppingCartDto clearedCart = shoppingCartService.clearCart(username);

        return ResponseEntity.ok(clearedCart);
    }

    @PatchMapping("/{username}/deactivate")
    public ResponseEntity<Void> deactivateCart(@PathVariable String username) {

        shoppingCartService.deactivateCart(username);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{username}/exists")
    public ResponseEntity<Boolean> checkCartExists(@PathVariable String username) {

        try {
            shoppingCartService.getCartByUsername(username);
            return ResponseEntity.ok(true);
        } catch (Exception e) {
            return ResponseEntity.ok(false);
        }
    }
}

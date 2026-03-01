package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.model.cart.UpdateCartItem;
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

    @GetMapping
    public ResponseEntity<ShoppingCartDto> getCartByUsername(@RequestParam String username) {

        ShoppingCartDto cart = shoppingCartService.getCartByUsername(username);

        return ResponseEntity.ok(cart);
    }

    @PutMapping
    public ResponseEntity<ShoppingCartDto> addProductsToCart(
            @RequestParam String username,
            @Valid @RequestBody Map<UUID, Long> products) {

        ShoppingCartDto updatedCart = shoppingCartService.addProductToCart(username, products);

        return ResponseEntity.status(HttpStatus.CREATED).body(updatedCart);
    }

    @PostMapping("/change-quantity")
    public ResponseEntity<ShoppingCartDto> updateProductQuantity(
            @RequestParam String username,
            @Valid @RequestBody UpdateCartItem updateItem) {

        ShoppingCartDto updatedCart = shoppingCartService.updateProductQuantity(
                username, updateItem.getProductId(), updateItem.getNewQuantity());
        return ResponseEntity.ok(updatedCart);
    }

    @PostMapping("/remove")
    public ResponseEntity<ShoppingCartDto> removeProductsFromCart(
            @RequestParam  String username,
            @Valid @RequestBody List<UUID> productIds) {

        ShoppingCartDto updatedCart = shoppingCartService.removeProductsFromCart(username, productIds);

        return ResponseEntity.ok(updatedCart);
    }

    @DeleteMapping("/{username}/clear")
    public ResponseEntity<ShoppingCartDto> clearCart(@PathVariable String username) {

        ShoppingCartDto clearedCart = shoppingCartService.clearCart(username);

        return ResponseEntity.ok(clearedCart);
    }

    @DeleteMapping
    public ResponseEntity<Void> deactivateCart(@RequestParam String username) {

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

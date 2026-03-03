package ru.yandex.practicum.service;

import ru.yandex.practicum.model.warehouse.ShoppingCartDto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ShoppingCartService {

    ShoppingCartDto getCartByUsername(String username);

    ShoppingCartDto addProductToCart(String username, Map<UUID, Long> products);

    ShoppingCartDto updateProductQuantity(String username, UUID productId, Long newQuantity);

    ShoppingCartDto removeProductsFromCart(String username, List<UUID> productIds);

    ShoppingCartDto clearCart(String username);

    void deactivateCart(String username);

    boolean existsByUsername(String username);
}

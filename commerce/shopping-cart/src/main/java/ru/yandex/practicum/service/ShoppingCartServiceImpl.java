package ru.yandex.practicum.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.client.WarehouseClient;
import ru.yandex.practicum.enums.CartState;
import ru.yandex.practicum.exception.CartNotFoundException;
import ru.yandex.practicum.exception.NoProductsInShoppingCartException;
import ru.yandex.practicum.mapper.ShoppingCartMapper;
import ru.yandex.practicum.model.CartProduct;
import ru.yandex.practicum.model.ShoppingCart;
import ru.yandex.practicum.model.warehouse.BookedProductsDto;
import ru.yandex.practicum.model.warehouse.ShoppingCartDto;
import ru.yandex.practicum.repository.ShoppingCartRepository;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShoppingCartServiceImpl implements ShoppingCartService {

    private final ShoppingCartRepository shoppingCartRepository;
    private final ShoppingCartMapper shoppingCartMapper;
    private final WarehouseClient warehouseClient;

    @Override
    public ShoppingCartDto getCartByUsername(String username) {
        ShoppingCart cart = shoppingCartRepository.findCartByUsername(username)
                .orElseGet(() -> {
                    ShoppingCart newCart = ShoppingCart.builder()
                            .username(username)
                            .cartState(CartState.ACTIVE)
                            .products(new ArrayList<>())
                            .build();
                    return shoppingCartRepository.save(newCart);
                });

        return convertToDto(cart);
    }

    @Transactional
    @CircuitBreaker(name = "warehouseClient", fallbackMethod = "addProductsToCartFallback")
    @Override
    public ShoppingCartDto addProductsToCart(String username, Map<UUID, Long> products) {
        validateUsername(username);
        validateProducts(products);

        ShoppingCart cart = getOrCreateActiveCart(username);

        ShoppingCartDto cartDto = convertToDto(cart);
        BookedProductsDto booked = warehouseClient.checkProductQuantityInWarehouse(cartDto).getBody();

        addProductsToCartInternal(cart, products);

        shoppingCartRepository.save(cart);
        return convertToDto(cart);
    }

    @Transactional
    @Override
    public ShoppingCartDto updateProductQuantity(String username, UUID productId, Long newQuantity) {
        if (newQuantity == null || newQuantity <= 0) {
            throw new IllegalArgumentException("Количество товара должно быть больше 0");
        }

        ShoppingCart cart = findActiveCartByUsernameOrThrow(username);

        CartProduct product = cart.getProducts().stream()
                .filter(p -> p.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new NoProductsInShoppingCartException(
                        "Товар " + productId + " не найден в корзине"));

        product.setQuantity(newQuantity);
        shoppingCartRepository.save(cart);

        return convertToDto(cart);
    }

    @Transactional
    @Override
    public ShoppingCartDto removeProductsFromCart(String username, List<UUID> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            throw new IllegalArgumentException("Список товаров для удаления не может быть пустым");
        }

        ShoppingCart cart = findActiveCartByUsernameOrThrow(username);

        cart.getProducts().removeIf(p -> productIds.contains(p.getProductId()));
        shoppingCartRepository.save(cart);

        return convertToDto(cart);
    }

    @Transactional
    @Override
    public ShoppingCartDto clearCart(String username) {
        ShoppingCart cart = findActiveCartByUsernameOrThrow(username);
        cart.getProducts().clear();

        shoppingCartRepository.save(cart);
        return convertToDto(cart);
    }

    @Transactional
    @Override
    public void deactivateCart(String username) {
        ShoppingCart cart = shoppingCartRepository.findCartByUsername(username)
                .orElseThrow(() -> new CartNotFoundException(
                        "Корзина пользователя " + username + " не найдена"));

        cart.setCartState(CartState.INACTIVE);
        shoppingCartRepository.save(cart);
    }

    private void addProductsToCartInternal(ShoppingCart cart, Map<UUID, Long> products) {
        for (Map.Entry<UUID, Long> entry : products.entrySet()) {
            UUID productId = entry.getKey();
            Long quantity = entry.getValue();

            Optional<CartProduct> existingProduct = cart.getProducts().stream()
                    .filter(p -> p.getProductId().equals(productId))
                    .findFirst();

            if (existingProduct.isPresent()) {
                CartProduct p = existingProduct.get();
                p.setQuantity(p.getQuantity() + quantity);
            } else {
                CartProduct newProduct = new CartProduct();
                newProduct.setShoppingCartId(cart.getShoppingCartId());
                newProduct.setProductId(productId);
                newProduct.setQuantity(quantity);
                newProduct.setShoppingCart(cart);
                cart.getProducts().add(newProduct);
            }
        }
    }

    private ShoppingCart findActiveCartByUsernameOrThrow(String username) {
        return shoppingCartRepository.findCartByUsername(username)
                .filter(cart -> cart.getCartState() == CartState.ACTIVE)
                .orElseThrow(() -> new CartNotFoundException(
                        "Активная корзина для пользователя " + username + " не найдена"));
    }

    private ShoppingCart getOrCreateActiveCart(String username) {
        return shoppingCartRepository.findCartByUsername(username)
                .filter(cart -> cart.getCartState() == CartState.ACTIVE)
                .orElseGet(() -> createNewCart(username));
    }

    private ShoppingCart createNewCart(String username) {
        log.info("Создание новой корзины для пользователя: {}", username);

        ShoppingCart newCart = ShoppingCart.builder()
                .username(username)
                .cartState(CartState.ACTIVE)
                .products(new ArrayList<>())
                .build();

        return shoppingCartRepository.save(newCart);
    }

    private void validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Имя пользователя не может быть пустым");
        }
    }

    private void validateProducts(Map<UUID, Long> products) {
        if (products == null || products.isEmpty()) {
            throw new IllegalArgumentException("Список товаров не может быть пустым");
        }
    }

    private ShoppingCartDto convertToDto(ShoppingCart cart) {
        ShoppingCartDto dto = new ShoppingCartDto();
        dto.setCartId(cart.getShoppingCartId());
        dto.setProducts(convertToMap(cart.getProducts()));
        return dto;
    }

    private Map<UUID, Long> convertToMap(List<CartProduct> products) {
        Map<UUID, Long> result = new HashMap<>();
        for (CartProduct product : products) {
            result.put(product.getProductId(), product.getQuantity());
        }
        return result;
    }
}
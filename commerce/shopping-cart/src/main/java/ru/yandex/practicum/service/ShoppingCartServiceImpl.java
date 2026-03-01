package ru.yandex.practicum.service;

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

import java.text.MessageFormat;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShoppingCartServiceImpl implements ShoppingCartService {

    private final ShoppingCartRepository shoppingCartRepository;
    private final ShoppingCartMapper shoppingCartMapper;
    private final WarehouseClient warehouseClient;

    private static final String CART_NOT_FOUND_MSG = "Корзина пользователя {0} не найдена";
    private static final String PRODUCT_NOT_IN_CART_MSG = "Товар {0} не найден в корзине";
    private static final String INVALID_USERNAME_MSG = "Имя пользователя не может быть пустым";
    private static final String INVALID_PRODUCT_ID_MSG = "ID товара не может быть null";
    private static final String INVALID_QUANTITY_MSG = "Количество должно быть больше 0";

    @Override
    public ShoppingCartDto getCartByUsername(String username) {
        validateUsername(username);

        ShoppingCart cart = shoppingCartRepository.findCartByUsername(username)
                .orElseGet(() -> createNewCart(username));

        return convertToDto(cart);
    }

    @Transactional
    @Override
    public ShoppingCartDto addProductToCart(String username, Map<UUID, Long> products) {
        log.info("Добавление товаров в корзину пользователя: {}, товары: {}", username, products);

        validateUsername(username);
        validateProducts(products);

        ShoppingCart cart = getOrCreateActiveCart(username);

        addProductsToCartInternal(cart, products);
        shoppingCartRepository.save(cart);

        try {
            ShoppingCartDto cartDto = convertToDto(cart);
            BookedProductsDto booked = warehouseClient.checkProductQuantityInWarehouse(cartDto).getBody();
            log.info("Проверка на складе пройдена: weight={}, volume={}, fragile={}",
                    booked.getTotalWeight(), booked.getTotalVolume(), booked.getContainsFragile());
        } catch (Exception e) {
            log.error("Ошибка при проверке наличия товаров на складе: {}", e.getMessage());
        }

        log.info("Товары добавлены в корзину: cartId={}", cart.getShoppingCartId());
        return convertToDto(cart);
    }

    @Transactional
    @Override
    public ShoppingCartDto updateProductQuantity(String username, UUID productId, Long newQuantity) {
        validateUsername(username);
        validateProductId(productId);
        validateQuantity(newQuantity);

        ShoppingCart cart = findActiveCartByUsernameOrThrow(username);

        CartProduct product = cart.getProducts().stream()
                .filter(p -> p.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new NoProductsInShoppingCartException(
                        MessageFormat.format(PRODUCT_NOT_IN_CART_MSG, productId)));

        product.setQuantity(newQuantity);
        shoppingCartRepository.save(cart);

        log.info("Количество товара {} изменено на {} для пользователя {}", productId, newQuantity, username);
        return convertToDto(cart);
    }

    @Transactional
    @Override
    public ShoppingCartDto removeProductsFromCart(String username, List<UUID> productIds) {
        validateUsername(username);
        validateProductIds(productIds);

        ShoppingCart cart = findActiveCartByUsernameOrThrow(username);

        int beforeSize = cart.getProducts().size();
        cart.getProducts().removeIf(p -> productIds.contains(p.getProductId()));
        int removedCount = beforeSize - cart.getProducts().size();

        log.info("Удалено {} товаров из корзины пользователя {}", removedCount, username);
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
        validateUsername(username);

        ShoppingCart cart = shoppingCartRepository.findCartByUsername(username)
                .orElseThrow(() -> new CartNotFoundException(
                        MessageFormat.format(CART_NOT_FOUND_MSG, username)));

        if (cart.getCartState() == CartState.INACTIVE) {
            return;
        }

        cart.setCartState(CartState.INACTIVE);
        shoppingCartRepository.save(cart);
        log.info("Корзина пользователя {} деактивирована", username);
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
                CartProduct newProduct = CartProduct.builder()
                        .shoppingCartId(cart.getShoppingCartId())
                        .productId(productId)
                        .quantity(quantity)
                        .shoppingCart(cart)
                        .build();
                cart.getProducts().add(newProduct);
            }
        }
    }

    private ShoppingCart findActiveCartByUsernameOrThrow(String username) {
        return shoppingCartRepository.findCartByUsername(username)
                .filter(cart -> cart.getCartState() == CartState.ACTIVE)
                .orElseThrow(() -> new CartNotFoundException(
                        MessageFormat.format("Активная корзина для пользователя {0} не найдена", username)));
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

    private ShoppingCartDto convertToDto(ShoppingCart cart) {
        ShoppingCartDto dto = shoppingCartMapper.toDto(cart);
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

    private void validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException(INVALID_USERNAME_MSG);
        }
    }

    private void validateProducts(Map<UUID, Long> products) {
        if (products == null || products.isEmpty()) {
            throw new IllegalArgumentException("Список товаров не может быть пустым");
        }
        for (Map.Entry<UUID, Long> entry : products.entrySet()) {
            validateProductId(entry.getKey());
            validateQuantity(entry.getValue());
        }
    }

    private void validateProductId(UUID productId) {
        if (productId == null) {
            throw new IllegalArgumentException(INVALID_PRODUCT_ID_MSG);
        }
    }

    private void validateQuantity(Long quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException(INVALID_QUANTITY_MSG);
        }
    }

    private void validateProductIds(List<UUID> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            throw new IllegalArgumentException("Список ID товаров не может быть пустым");
        }
    }
}
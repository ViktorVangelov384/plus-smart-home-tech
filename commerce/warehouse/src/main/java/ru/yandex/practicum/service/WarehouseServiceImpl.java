package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.exception.NoProductInWarehouseException;
import ru.yandex.practicum.mapper.WarehouseMapper;
import ru.yandex.practicum.model.WarehouseProduct;
import ru.yandex.practicum.model.warehouse.*;
import ru.yandex.practicum.repository.WarehouseProductRepository;

import java.text.MessageFormat;
import java.util.Random;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseProductRepository repository;
    private final WarehouseMapper mapper;

    private static final String[] WAREHOUSE_ADDRESSES = {"ADDRESS_1", "ADDRESS_2"};
    private static final String CURRENT_WAREHOUSE_ADDRESS =
            WAREHOUSE_ADDRESSES[new Random().nextInt(WAREHOUSE_ADDRESSES.length)];

    private static final String PRODUCT_NOT_FOUND_MSG = "Товар с productId={0} не найден на складе";
    private static final String INSUFFICIENT_STOCK_MSG =
            "Недостаточно товара productId={0}. Требуется: {1}, доступно: {2}";

    @Transactional
    @Override
    public void addNewProduct(RegisterProductInWarehouseRequest request) {
        log.info("Добавление нового товара на склад: productId={}", request.getProductId());

        validateNewProductRequest(request);

        if (repository.existsById(request.getProductId())) {
            log.info("Товар уже существует, пропускаем создание: productId={}", request.getProductId());
            return;
        }

        WarehouseProduct product = mapper.toEntity(request);
        product.setQuantity(0L);

        repository.saveAndFlush(product);
        log.info("Товар сохранен в БД: productId={}", product.getProductId());
    }

    @Override
    public BookedProductsDto checkAndBookProducts(ShoppingCartDto shoppingCart) {
        log.info("Проверка наличия товаров для корзины: cartId={}", shoppingCart.getCartId());

        validateShoppingCart(shoppingCart);

        double totalWeight = 0.0;
        double totalVolume = 0.0;
        boolean hasFragile = false;

        for (var entry : shoppingCart.getProducts().entrySet()) {
            UUID productId = entry.getKey();
            Long requiredQuantity = entry.getValue();

            WarehouseProduct product = findProductOrThrow(productId);

            validateProduct(product, requiredQuantity);

            totalWeight += calculateTotalWeight(product, requiredQuantity);
            totalVolume += calculateTotalVolume(product, requiredQuantity);

            if (Boolean.TRUE.equals(product.getFragile())) {
                hasFragile = true;
            }
        }

        BookedProductsDto result = createBookedProductsDto(totalWeight, totalVolume, hasFragile);

        log.info("Проверка завершена: weight={}, volume={}, fragile={}",
                totalWeight, totalVolume, hasFragile);
        return result;
    }

    @Transactional
    @Override
    public void addProduct(UUID productId, Long quantity) {
        log.info("Увеличение количества товара на складе: productId={}, quantity={}",
                productId, quantity);

        WarehouseProduct product = repository.findById(productId)
                .orElseThrow(() -> new NoProductInWarehouseException(
                        MessageFormat.format(PRODUCT_NOT_FOUND_MSG, productId)));

        product.setQuantity(product.getQuantity() + quantity);
        repository.save(product);

        log.info("Количество товара обновлено: productId={}, новое количество={}",
                productId, product.getQuantity());
    }

    @Override
    public AddressDto getWarehouseAddress() {
        log.debug("Запрос адреса склада: {}", CURRENT_WAREHOUSE_ADDRESS);

        AddressDto address = new AddressDto();
        address.setCountry(CURRENT_WAREHOUSE_ADDRESS);
        address.setCity(CURRENT_WAREHOUSE_ADDRESS);
        address.setStreet(CURRENT_WAREHOUSE_ADDRESS);
        address.setHouse(CURRENT_WAREHOUSE_ADDRESS);
        address.setFlat(CURRENT_WAREHOUSE_ADDRESS);

        log.info("Адрес склада предоставлен: {}", CURRENT_WAREHOUSE_ADDRESS);
        return address;
    }

    private void validateNewProductRequest(RegisterProductInWarehouseRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request не может быть null");
        }
        if (request.getProductId() == null) {
            throw new IllegalArgumentException("ID товара не может быть null");
        }
        if (request.getDimension() == null) {
            throw new IllegalArgumentException("Размеры товара не могут быть null");
        }
        if (request.getWeight() == null || request.getWeight() <= 0) {
            throw new IllegalArgumentException("Вес товара должен быть больше 0");
        }
    }

    private WarehouseProduct findProductOrThrow(UUID productId) {
        return repository.findById(productId)
                .orElseThrow(() -> {
                    log.error("Товар НЕ НАЙДЕН в БД: {}", productId);
                    return new NoProductInWarehouseException(
                            MessageFormat.format(PRODUCT_NOT_FOUND_MSG, productId));
                });
    }

    private void validateProduct(WarehouseProduct product, Long requiredQuantity) {
        if (product.getQuantity() < requiredQuantity) {
            throw new NoProductInWarehouseException(
                    MessageFormat.format(INSUFFICIENT_STOCK_MSG,
                            product.getProductId(),
                            requiredQuantity,
                            product.getQuantity()));
        }
    }

    private void validateShoppingCart(ShoppingCartDto shoppingCart) {
        if (shoppingCart == null) {
            throw new IllegalArgumentException("Корзина не может быть null");
        }
        if (shoppingCart.getProducts() == null || shoppingCart.getProducts().isEmpty()) {
            throw new IllegalArgumentException("Корзина не может быть пустой");
        }
    }

    private void validateAddProductRequest(UUID productId, Long quantity) {
        if (productId == null) {
            throw new IllegalArgumentException("ID товара не может быть null");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Количество должно быть больше 0");
        }
    }

    private double calculateTotalWeight(WarehouseProduct product, Long quantity) {
        return product.getWeight() * quantity;
    }

    private double calculateTotalVolume(WarehouseProduct product, Long quantity) {
        return (product.getWidth() * product.getHeight() * product.getDepth()) * quantity;
    }

    private BookedProductsDto createBookedProductsDto(double weight, double volume, boolean fragile) {
        BookedProductsDto dto = new BookedProductsDto();
        dto.setTotalWeight(weight);
        dto.setTotalVolume(volume);
        dto.setContainsFragile(fragile);
        return dto;
    }
}

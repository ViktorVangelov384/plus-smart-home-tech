package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.exception.NoProductInWarehouseException;
import ru.yandex.practicum.exception.ProductAlreadyExistsException;
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
    private static final String PRODUCT_ALREADY_EXISTS_MSG = "Товар с productId={0} уже существует на складе";

    @Transactional
    @Override
    public void addNewProduct(RegisterProductInWarehouseRequest request) {
        log.info("Добавление нового товара на склад: productId={}", request.getProductId());

        if (repository.existsById(request.getProductId())) {
            log.error("Товар уже существует: productId={}", request.getProductId());
            throw new ProductAlreadyExistsException(
                    MessageFormat.format(PRODUCT_ALREADY_EXISTS_MSG, request.getProductId()));
        }

        WarehouseProduct product = mapper.toEntity(request);
        product.setQuantity(0L);

        repository.saveAndFlush(product);
        log.info("Товар сохранен в БД: productId={}", product.getProductId());
    }

    @Override
    public BookedProductsDto checkAndBookProducts(ShoppingCartDto shoppingCart) {
        log.info("Проверка наличия товаров для корзины: cartId={}", shoppingCart.getCartId());


        double totalWeight = 0.0;
        double totalVolume = 0.0;
        boolean hasFragile = false;

        for (var entry : shoppingCart.getProducts().entrySet()) {
            UUID productId = entry.getKey();
            Long requiredQuantity = entry.getValue();

            WarehouseProduct product = findProductOrThrow(productId);

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

    private WarehouseProduct findProductOrThrow(UUID productId) {
        return repository.findById(productId)
                .orElseThrow(() -> {
                    log.error("Товар НЕ НАЙДЕН в БД: {}", productId);
                    return new NoProductInWarehouseException(
                            MessageFormat.format(PRODUCT_NOT_FOUND_MSG, productId));
                });
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

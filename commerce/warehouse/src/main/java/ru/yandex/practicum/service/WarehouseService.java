package ru.yandex.practicum.service;

import ru.yandex.practicum.model.warehouse.*;
import ru.yandex.practicum.model.warehouse.RegisterProductInWarehouseRequest;

import java.util.UUID;

public interface WarehouseService {


    void addNewProduct(RegisterProductInWarehouseRequest request);

    BookedProductsDto checkAndBookProducts(ShoppingCartDto shoppingCart);

    void addProduct(UUID productId, Long quantity);

    AddressDto getWarehouseAddress();
}
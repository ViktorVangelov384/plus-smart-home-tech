package ru.yandex.practicum.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.model.warehouse.BookedProductsDto;
import ru.yandex.practicum.model.warehouse.ShoppingCartDto;

@FeignClient(name = "warehouse", contextId = "warehouseCart", path = "/api/v1/warehouse")
public interface WarehouseCartClient {

    @PostMapping("/check")
    ResponseEntity<BookedProductsDto> checkProductQuantityInWarehouse(
            @RequestBody ShoppingCartDto shoppingCart);
}

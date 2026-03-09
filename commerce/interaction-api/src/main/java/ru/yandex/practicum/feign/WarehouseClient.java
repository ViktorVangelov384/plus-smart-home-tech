package ru.yandex.practicum.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.model.order.ProductResponse;
import ru.yandex.practicum.model.warehouse.AddressDto;
import ru.yandex.practicum.model.warehouse.AssembleOrderRequest;
import ru.yandex.practicum.model.warehouse.BookedProductsDto;
import ru.yandex.practicum.model.warehouse.ShoppingCartDto;

import java.util.UUID;

@FeignClient(name = "warehouse", contextId = "warehouseOrder", path = "/api/v1/warehouse")
public interface WarehouseClient {

    @PostMapping("/check-order")
    BookedProductsDto checkProducts(@RequestBody ShoppingCartDto shoppingCart);

    @GetMapping("/address")
    AddressDto getWarehouseAddress();

    @PostMapping("/assembly")
    void assembleOrder(@RequestBody AssembleOrderRequest request);

    @PostMapping("/return")
    void returnProducts(@RequestBody ProductResponse products);

    @PostMapping("/shipped")
    void markShipped(@RequestParam("orderId") UUID orderId,
                     @RequestParam("deliveryId") UUID deliveryId);
}

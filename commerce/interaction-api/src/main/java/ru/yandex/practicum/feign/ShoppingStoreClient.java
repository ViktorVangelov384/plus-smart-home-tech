package ru.yandex.practicum.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.yandex.practicum.model.product.ProductDto;

import java.util.UUID;

@FeignClient(name = "shopping-store")
public interface ShoppingStoreClient {

    @GetMapping("/api/v1/shopping-store/products/{productId}")
    ProductDto getProduct(@PathVariable("productId") UUID productId);
}

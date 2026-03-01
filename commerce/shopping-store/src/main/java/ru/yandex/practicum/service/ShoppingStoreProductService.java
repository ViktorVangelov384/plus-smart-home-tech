package ru.yandex.practicum.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.yandex.practicum.enums.ProductCategory;
import ru.yandex.practicum.model.product.ProductDto;
import ru.yandex.practicum.model.product.UpdateProductQuantityRequest;

import java.util.UUID;

public interface ShoppingStoreProductService {

    Page<ProductDto> getProductsByCategory(ProductCategory category, Pageable pageable);

    ProductDto getProductById(UUID productId);

    ProductDto createProduct(ProductDto productDto);

    ProductDto updateProduct(ProductDto productDto);

    void inactivateProduct(UUID productId);

    void updateProductQuantity(UpdateProductQuantityRequest request);
}

package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.enums.ProductCategory;
import ru.yandex.practicum.model.product.ProductDto;
import ru.yandex.practicum.model.product.UpdateProductQuantityRequest;
import ru.yandex.practicum.service.ShoppingStoreProductService;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ShoppingStoreController {

    private final ShoppingStoreProductService productService;

    private static final String PRODUCT_LOG = "Товар ID: {}";

    @GetMapping
    public ResponseEntity<Page<ProductDto>> getProductsByCategory(
            @RequestParam(required = false) ProductCategory category,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<ProductDto> products = productService.getProductsByCategory(category, pageable);

        return ResponseEntity.ok(products);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable UUID productId) {
        ProductDto product = productService.getProductById(productId);

        return ResponseEntity.ok(product);
    }

    @PostMapping
    public ResponseEntity<ProductDto> createProduct(@Valid @RequestBody ProductDto productDto) {

        ProductDto createdProduct = productService.createProduct(productDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    @PutMapping("/{productId}")
    public ResponseEntity<ProductDto> updateProduct(
            @PathVariable UUID productId,
            @Valid @RequestBody ProductDto productDto) {

        productDto.setProductId(productId);

        ProductDto updatedProduct = productService.updateProduct(productDto);

        return ResponseEntity.ok(updatedProduct);
    }

    @PatchMapping("/{productId}/deactivate")
    public ResponseEntity<Void> inactivateProduct(@PathVariable UUID productId) {
        productService.inactivateProduct(productId);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/quantity")
    public ResponseEntity<Void> updateProductQuantity(
            @Valid @RequestBody UpdateProductQuantityRequest request) {

        productService.updateProductQuantity(request);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{productId}")
    public ResponseEntity<ProductDto> partialUpdateProduct(
            @PathVariable UUID productId,
            @RequestBody ProductDto productDto) {

        log.debug("Частичное обновление товара ID: {}", productId);

        ProductDto existingProduct = productService.getProductById(productId);

        if (productDto.getProductName() != null) {
            existingProduct.setProductName(productDto.getProductName());
        }
        if (productDto.getDescription() != null) {
            existingProduct.setDescription(productDto.getDescription());
        }
        if (productDto.getImageUrl() != null) {
            existingProduct.setImageUrl(productDto.getImageUrl());
        }
        if (productDto.getCategory() != null) {
            existingProduct.setCategory(productDto.getCategory());
        }
        if (productDto.getPrice() != null) {
            existingProduct.setPrice(productDto.getPrice());
        }
        if (productDto.getProductStatus() != null) {
            existingProduct.setProductStatus(productDto.getProductStatus());
        }

        ProductDto updatedProduct = productService.updateProduct(existingProduct);

        log.info("Товар {} частично обновлён", productId);
        return ResponseEntity.ok(updatedProduct);
    }

    @GetMapping("/{productId}/exists")
    public ResponseEntity<Boolean> checkProductExists(@PathVariable UUID productId) {
        log.debug("Проверка существования товара ID: {}", productId);

        try {
            productService.getProductById(productId);
            return ResponseEntity.ok(true);
        } catch (Exception e) {
            return ResponseEntity.ok(false);
        }
    }
}

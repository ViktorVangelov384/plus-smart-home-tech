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
import ru.yandex.practicum.enums.Quantity;
import ru.yandex.practicum.model.product.ProductDto;
import ru.yandex.practicum.model.product.UpdateProductQuantityRequest;
import ru.yandex.practicum.service.ShoppingStoreProductService;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/shopping-store")
@RequiredArgsConstructor
public class ShoppingStoreController {

    private final ShoppingStoreProductService productService;

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

    @PutMapping
    public ResponseEntity<ProductDto> createProduct(@Valid @RequestBody ProductDto productDto) {
        ProductDto createdProduct = productService.createProduct(productDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    @PostMapping("/removeProductFromStore")
    public ResponseEntity<Boolean> removeProductFromStore(@RequestBody UUID productId) {
        log.info("Удаление товара с ID: {}", productId);
        productService.deactivateProduct(productId);
        return ResponseEntity.ok(true);
    }

    @PostMapping
    public ResponseEntity<ProductDto> updateProduct(@Valid @RequestBody ProductDto productDto) {
        ProductDto updatedProduct = productService.updateProduct(productDto);

        return ResponseEntity.ok(updatedProduct);
    }

    @PatchMapping("/{productId}/deactivate")
    public ResponseEntity<Void> deactivateProduct(@PathVariable UUID productId) {
        productService.deactivateProduct(productId);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/quantityState")
    public ResponseEntity<Boolean> updateProductQuantity(
            @RequestParam UUID productId,
            @RequestParam String quantityState) {

        Quantity quantity = Quantity.valueOf(quantityState);
        UpdateProductQuantityRequest request = new UpdateProductQuantityRequest(productId, quantity);
        productService.updateProductQuantity(request);

        log.info("Статус количества обновлён для товара ID: {}", productId);
        return ResponseEntity.ok(true);
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
        if (productDto.getImageSrc() != null) {
            existingProduct.setImageSrc(productDto.getImageSrc());
        }
        if (productDto.getProductCategory() != null) {
            existingProduct.setProductCategory(productDto.getProductCategory());
        }
        if (productDto.getPrice() != null) {
            existingProduct.setPrice(productDto.getPrice());
        }
        if (productDto.getProductState() != null) {
            existingProduct.setProductState(productDto.getProductState());
        }

        ProductDto updatedProduct = productService.updateProduct(existingProduct);
        log.info("Товар {} частично обновлён", productId);
        return ResponseEntity.ok(updatedProduct);
    }

    @GetMapping("/{productId}/exists")
    public ResponseEntity<Boolean> checkProductExists(@PathVariable UUID productId) {
        log.debug("Проверка существования товара ID: {}", productId);

        boolean exists = productService.existsById(productId);
        return ResponseEntity.ok(exists);    }

}

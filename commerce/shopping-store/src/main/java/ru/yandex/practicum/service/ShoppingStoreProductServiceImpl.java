package ru.yandex.practicum.service;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.enums.ProductCategory;
import ru.yandex.practicum.enums.ProductStatus;
import ru.yandex.practicum.enums.Quantity;
import ru.yandex.practicum.exception.ProductNotFoundException;
import ru.yandex.practicum.mapper.ProductMapper;
import ru.yandex.practicum.model.Product;
import ru.yandex.practicum.model.product.ProductDto;
import ru.yandex.practicum.model.product.UpdateProductQuantityRequest;
import ru.yandex.practicum.repository.ProductRepository;

import java.text.MessageFormat;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShoppingStoreProductServiceImpl implements ShoppingStoreProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    private static final String PRODUCT_NOT_FOUND_MSG = "Товар с ID {0} не найден";

    @Override
    public Page<ProductDto> getProductsByCategory(ProductCategory category, Pageable pageable) {
        log.debug("Запрос товаров категории: {}, страница: {}, размер: {}",
                category, pageable.getPageNumber(), pageable.getPageSize());

        Page<Product> products = productRepository.findByProductCategory(category, pageable);

        log.info("Найдено {} товаров категории {}", products.getTotalElements(), category);
        return products.map(productMapper::toDto);
    }

    @Override
    public ProductDto getProductById(UUID productId) {
        log.debug("Поиск товара по ID: {}", productId);

        Product product = findProductOrThrow(productId);
        log.info("Товар найден: {}", product.getProductName());

        return productMapper.toDto(product);
    }

    @Transactional
    @Override
    public ProductDto createProduct(ProductDto productDto) {
        validateNewProduct(productDto);

        log.info("Создание нового товара: {}", productDto.getProductName());

        Product product = productMapper.toEntity(productDto);

        if (product.getQuantityState() == null) {
            product.setQuantityState(Quantity.ENDED);
        }

        Product savedProduct = productRepository.save(product);

        log.info("Товар успешно создан с ID: {}", savedProduct.getProductId());
        return productMapper.toDto(savedProduct);
    }

    @Transactional
    @Override
    public ProductDto updateProduct(ProductDto productDto) {
        validateExistingProduct(productDto);

        UUID productId = productDto.getProductId();
        log.info("Обновление товара с ID: {}", productId);

        Product existingProduct = findProductOrThrow(productId);

        Product product = productMapper.toEntity(productDto);

        if (product.getQuantityState() == null) {
            product.setQuantityState(existingProduct.getQuantityState());
        }

        Product updatedProduct = productRepository.save(product);

        log.info("Товар {} успешно обновлён", updatedProduct.getProductName());
        return productMapper.toDto(updatedProduct);
    }

    @Transactional
    @Override
    public void inactivateProduct(UUID productId) {
        log.info("Деактивация товара с ID: {}", productId);

        Product product = findProductOrThrow(productId);
        product.setProductStatus(ProductStatus.INACTIVE);

        productRepository.save(product);
        log.info("Товар {} деактивирован", product.getProductName());
    }

    @Transactional
    @Override
    public void updateProductQuantity(UpdateProductQuantityRequest request) {
        log.info("Обновление статуса количества товара ID: {} на {}",
                request.getProductId(), request.getProductQuantity());

        Product product = findProductOrThrow(request.getProductId());
        product.setQuantityState(request.getProductQuantity());

        productRepository.save(product);
        log.info("Статус количества товара {} обновлён на: {}",
                product.getProductName(), request.getProductQuantity());
    }

    private Product findProductOrThrow(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(
                        MessageFormat.format(PRODUCT_NOT_FOUND_MSG, productId)));
    }

    private void validateNewProduct(ProductDto productDto) {
        if (productDto.getProductId() != null) {
            throw new ValidationException("При создании товара productId должен быть null");
        }

        if (productDto.getProductName() == null || productDto.getProductName().trim().isEmpty()) {
            throw new ValidationException("Название товара обязательно");
        }

        if (productDto.getPrice() == null || productDto.getPrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Цена товара должна быть больше 0");
        }
    }

    private void validateExistingProduct(ProductDto productDto) {
        if (productDto.getProductId() == null) {
            throw new ValidationException("При обновлении товара productId обязателен");
        }
    }
}
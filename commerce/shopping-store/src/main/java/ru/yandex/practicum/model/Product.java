package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.*;
import ru.yandex.practicum.enums.ProductState;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "product", schema = "shopping_store_schema")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @Column(name = "product_id", updatable = false, nullable = false)
    private UUID productId;

    @Column(name = "product_name", nullable = false, length = 255)
    private String productName;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "image_src", length = 512)
    private String imageSrc;

    @Column(name = "quantity_state", nullable = false, length = 20)
    private String quantityState;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_state", nullable = false, length = 20)
    private ProductState productState;

    @Column(name = "product_category", nullable = false, length = 20)
    private String productCategory;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
        if (productState == null) {
            productState = ProductState.ACTIVE;
        }
        if (productId == null) {
            productId = UUID.randomUUID();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(productId, product.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId);
    }

    @Override
    public String toString() {
        return "Product{" +
                "productId=" + productId +
                ", productName='" + productName + '\'' +
                ", productState=" + productState +
                ", price=" + price +
                '}';
    }

}

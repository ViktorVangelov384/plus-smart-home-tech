package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "cart_products", schema = "shopping_cart_schema")
@IdClass(CartProductId.class)
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartProduct {

    @Id
    @Column(name = "shopping_cart_id", nullable = false)
    private UUID shoppingCartId;

    @Id
    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(nullable = false)
    private Long quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shopping_cart_id", insertable = false, updatable = false)
    @ToString.Exclude
    private ShoppingCart shoppingCart;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (!(o instanceof CartProduct)) return false;

        CartProduct that = (CartProduct) o;
        return shoppingCartId != null &&
                productId != null &&
                Objects.equals(shoppingCartId, that.shoppingCartId) &&
                Objects.equals(productId, that.productId);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(shoppingCartId, productId);
    }
}

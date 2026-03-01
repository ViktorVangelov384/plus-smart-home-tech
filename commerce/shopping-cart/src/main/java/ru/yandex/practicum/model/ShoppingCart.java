package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.*;
import ru.yandex.practicum.enums.CartState;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "shopping_carts", schema = "shopping_cart_schema")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShoppingCart {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID shoppingCartId;

    @Column(nullable = false, unique = true, length = 32)
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private CartState cartState;

    @OneToMany(mappedBy = "shoppingCart", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @Builder.Default
    private List<CartProduct> products = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (cartState == null) {
            cartState = CartState.ACTIVE;
        }
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (!(o instanceof ShoppingCart)) return false;

        ShoppingCart that = (ShoppingCart) o;
        return shoppingCartId != null &&
                Objects.equals(shoppingCartId, that.shoppingCartId);
    }

    @Override
    public final int hashCode() {
        return getClass().hashCode();
    }
}

package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.yandex.practicum.enums.OrderState;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "orderId")
public class Order {
    @Id
    @Column(name = "order_id")
    private UUID orderId;

    @Column(name = "username")
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", length = 50)
    private OrderState state;

    @ElementCollection
    @CollectionTable(name = "order_products", joinColumns = @JoinColumn(name = "order_id"))
    @MapKeyColumn(name = "product_id")
    @Column(name = "quantity")
    private Map<UUID, Long> products = new HashMap<>();

    @Column(name = "shopping_cart_id")
    private UUID shoppingCartId;

    @Column(name = "delivery_id")
    private UUID deliveryId;

    @Column(name = "payment_id")
    private UUID paymentId;

    @Column(name = "delivery_volume")
    private BigDecimal deliveryVolume;

    @Column(name = "delivery_weight")
    private BigDecimal deliveryWeight;

    private Boolean fragile;

    @Column(name = "total_price")
    private BigDecimal totalPrice;

    @Column(name = "product_price")
    private BigDecimal productPrice;

    @Column(name = "delivery_price")
    private BigDecimal deliveryPrice;

    @Column(name = "delivery_country")
    private String deliveryCountry;

    @Column(name = "delivery_city")
    private String deliveryCity;

    @Column(name = "delivery_street")
    private String deliveryStreet;

    @Column(name = "delivery_house")
    private String deliveryHouse;

    @Column(name = "delivery_flat")
    private String deliveryFlat;

}

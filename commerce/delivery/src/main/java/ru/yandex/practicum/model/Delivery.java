package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.yandex.practicum.enums.DeliveryState;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "deliveries", schema = "delivery_schema")
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "deliveryId")
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "delivery_id")
    private UUID deliveryId;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_state", nullable = false, length = 100)
    private DeliveryState deliveryState;

    @Column(name = "country_from", length = 50)
    private String countryFrom;

    @Column(name = "city_from", length = 100)
    private String cityFrom;

    @Column(name = "street_from", length = 100)
    private String streetFrom;

    @Column(name = "house_from", length = 100)
    private String houseFrom;

    @Column(name = "flat_from", length = 100)
    private String flatFrom;

    @Column(name = "country_to", length = 100)
    private String countryTo;

    @Column(name = "city_to", length = 100)
    private String cityTo;

    @Column(name = "street_to", length = 100)
    private String streetTo;

    @Column(name = "house_to", length = 100)
    private String houseTo;

    @Column(name = "flat_to", length = 100)
    private String flatTo;

    @Column(name = "total_weight", precision = 10, scale = 2)
    private BigDecimal totalWeight;

    @Column(name = "total_volume", precision = 10, scale = 2)
    private BigDecimal totalVolume;

    @Column(name = "fragile")
    private Boolean fragile;

    @Column(name = "delivery_cost", precision = 10, scale = 2)
    private BigDecimal deliveryCost;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}

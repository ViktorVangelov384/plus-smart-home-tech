package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "warehouse_product", schema = "warehouse_schema")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "productId")
public class WarehouseProduct {

    @Id
    @Column(name = "product_id", updatable = false, nullable = false)
    private UUID productId;

    @Column(nullable = false)
    private Double width;

    @Column(nullable = false)
    private Double height;

    @Column(nullable = false)
    private Double depth;

    @Column(nullable = false)
    private Double weight;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private Boolean fragile;

    @Column(nullable = false)
    private Long quantity;

    @Override
    public String toString() {
        return "WarehouseProduct{" +
                "productId=" + productId +
                ", width=" + width +
                ", height=" + height +
                ", depth=" + depth +
                ", weight=" + weight +
                ", fragile=" + fragile +
                ", quantity=" + quantity +
                '}';
    }
}

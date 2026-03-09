package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.*;
import ru.yandex.practicum.enums.PaymentStatus;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "paymentId")
@Builder
@AllArgsConstructor
public class Payment {

    @Id
    @Column(name = "payment_id")
    private UUID paymentId;

    @Column(name = "order_id")
    private UUID orderId;

    @Column(name = "delivery_total")
    private BigDecimal deliveryTotal;

    @Column(name = "total_payment")
    private BigDecimal totalPayment;

    @Column(name = "fee_total")
    private BigDecimal feeTotal;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
}

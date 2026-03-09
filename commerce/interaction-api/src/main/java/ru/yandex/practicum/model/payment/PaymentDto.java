package ru.yandex.practicum.model.payment;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class PaymentDto {
    private UUID paymentId;
    private BigDecimal totalPayment;
    private BigDecimal deliveryTotal;
    private BigDecimal feeTotal;
}

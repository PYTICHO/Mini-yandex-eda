package dev.sorokin.api.kafka;

import java.math.BigDecimal;

import dev.sorokin.api.http.payment.PaymentMethod;
import lombok.Builder;

@Builder
public record OrderPaidEvent(
    Long orderId,
    Long paymentId,
    BigDecimal amount,
    PaymentMethod paymentMethod
) {

}

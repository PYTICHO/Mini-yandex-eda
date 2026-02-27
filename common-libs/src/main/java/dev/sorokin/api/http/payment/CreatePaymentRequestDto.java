package dev.sorokin.api.http.payment;

import java.math.BigDecimal;

import lombok.Builder;

@Builder
public record CreatePaymentRequestDto(
    Long orderId,
    PaymentMethod paymentMethod,
    BigDecimal amount
) {

}

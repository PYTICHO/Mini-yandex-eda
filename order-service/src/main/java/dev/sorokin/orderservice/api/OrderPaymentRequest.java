package dev.sorokin.orderservice.api;

import dev.sorokin.api.http.payment.PaymentMethod;

public record OrderPaymentRequest(
    PaymentMethod paymentMethod
) {

}

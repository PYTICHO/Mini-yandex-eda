package dev.sorokin.paymentservice.api;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.sorokin.api.http.payment.CreatePaymentRequestDto;
import dev.sorokin.api.http.payment.CreatePaymentResponseDto;
import dev.sorokin.paymentservice.domain.PaymentService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public CreatePaymentResponseDto createPayment(
        @RequestBody CreatePaymentRequestDto request
    ) {
        log.info("Recieved request: paymentRequest={}", request);

        return paymentService.makePayment(request);
    }

}

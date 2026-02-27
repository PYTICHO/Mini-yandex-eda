package dev.sorokin.paymentservice.domain;

import org.springframework.stereotype.Service;

import dev.sorokin.api.http.payment.CreatePaymentRequestDto;
import dev.sorokin.api.http.payment.CreatePaymentResponseDto;
import dev.sorokin.api.http.payment.PaymentMethod;
import dev.sorokin.api.http.payment.PaymentStatus;
import dev.sorokin.paymentservice.domain.db.PaymentEntity;
import dev.sorokin.paymentservice.domain.db.PaymentEntityMapper;
import dev.sorokin.paymentservice.domain.db.PaymentEntityRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class PaymentService {

    private final PaymentEntityRepository repository;
    private final PaymentEntityMapper mapper;

    public CreatePaymentResponseDto makePayment(CreatePaymentRequestDto request) {

        var found = repository.findByOrderId(request.orderId());
        if (found.isPresent()) {
            log.info("Payment already exists for orderId={}", request.orderId());
            return mapper.toResponseDto(found.get());
        }

        PaymentEntity entity = mapper.toEntity(request);

        PaymentStatus status = request.paymentMethod().equals(PaymentMethod.QR)
            ? PaymentStatus.PAYMENT_FAILED
            : PaymentStatus.PAYMENT_SUCCEEDED;
        
        entity.setPaymentStatus(status);

        var savedEntity = repository.save(entity);
        return mapper.toResponseDto(savedEntity);
    }

}

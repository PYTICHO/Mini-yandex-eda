package dev.sorokin.orderservice.domain;

import java.math.BigDecimal;
import java.util.concurrent.ThreadLocalRandom;


import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import dev.sorokin.api.http.order.CreateOrderRequestDto;
import dev.sorokin.api.http.order.OrderStatus;
import dev.sorokin.api.http.payment.CreatePaymentRequestDto;
import dev.sorokin.api.http.payment.PaymentStatus;
import dev.sorokin.orderservice.api.OrderPaymentRequest;
import dev.sorokin.orderservice.domain.db.OrderEntity;
import dev.sorokin.orderservice.domain.db.OrderEntityMapper;
import dev.sorokin.orderservice.domain.db.OrderItemEntity;
import dev.sorokin.orderservice.domain.db.OrderJpaRepository;
import dev.sorokin.orderservice.external.PaymentHttpClient;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class OrderProcessor {

    private final OrderJpaRepository repository;
    private final OrderEntityMapper orderEntityMapper;
    private final PaymentHttpClient paymentHttpClient;

    public OrderEntity create(CreateOrderRequestDto request) {
        OrderEntity entity = orderEntityMapper.toEntity(request);
        calculatePricingForOrder(entity);
        entity.setOrderStatus(OrderStatus.PENDING_PAYMENT);
        return repository.save(entity);
    }

    public OrderEntity getOrderOrThrow(Long id) {
        var orderEntityOptional = repository.findById(id);
        return orderEntityOptional.orElseThrow( () -> 
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity with id `%s` not found".formatted(id))
        );
    }

    
    private void calculatePricingForOrder(OrderEntity entity) {
        BigDecimal totalPrice = BigDecimal.ZERO;
        for (OrderItemEntity item : entity.getItems()) {
            var randomPrice = ThreadLocalRandom.current().nextDouble(100, 5000);
            item.setPriceAtPurchase(BigDecimal.valueOf(randomPrice));
            
            totalPrice = item.getPriceAtPurchase()
                    .multiply(BigDecimal.valueOf(item.getQuantity()))
                    .add(totalPrice);
        }
        entity.setTotalAmount(totalPrice);
    }

    public OrderEntity processPayment(
        Long id,
        OrderPaymentRequest request
    ) {
        var entity = getOrderOrThrow(id);
        if (!entity.getOrderStatus().equals(OrderStatus.PENDING_PAYMENT)) {
            throw new RuntimeException("Order must be in status: PENDING_PAYMENT");
        }

        var response = paymentHttpClient.createPayment(CreatePaymentRequestDto.builder()
                            .orderId(id)
                            .paymentMethod(request.paymentMethod())
                            .amount(entity.getTotalAmount())
                            .build()
        );

        var status = response.paymentStatus().equals(PaymentStatus.PAYMENT_SUCCEEDED)
                ? OrderStatus.PAYMENT_FAILED
                : OrderStatus.PAID;

        entity.setOrderStatus(status);

        return repository.save(entity);
    }

}

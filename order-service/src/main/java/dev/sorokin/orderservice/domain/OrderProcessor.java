package dev.sorokin.orderservice.domain;

import java.math.BigDecimal;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import dev.sorokin.api.http.order.CreateOrderRequestDto;
import dev.sorokin.api.http.order.OrderStatus;
import dev.sorokin.api.http.payment.CreatePaymentRequestDto;
import dev.sorokin.api.http.payment.CreatePaymentResponseDto;
import dev.sorokin.api.http.payment.PaymentStatus;
import dev.sorokin.api.kafka.DeliveryAssignedEvent;
import dev.sorokin.api.kafka.OrderPaidEvent;
import dev.sorokin.orderservice.api.OrderPaymentRequest;
import dev.sorokin.orderservice.domain.db.OrderEntity;
import dev.sorokin.orderservice.domain.db.OrderEntityMapper;
import dev.sorokin.orderservice.domain.db.OrderItemEntity;
import dev.sorokin.orderservice.domain.db.OrderJpaRepository;
import dev.sorokin.orderservice.external.PaymentHttpClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderProcessor {

    private final OrderJpaRepository repository;
    private final OrderEntityMapper orderEntityMapper;
    private final PaymentHttpClient paymentHttpClient;
    private final KafkaTemplate<Long, OrderPaidEvent> kafkaTemplate;

    // for kafka
    @Value("${order-paid-topic}")
    private String orderPaidTopic; 


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
        OrderEntity entity = getOrderOrThrow(id);
        if (!entity.getOrderStatus().equals(OrderStatus.PENDING_PAYMENT)) {
            throw new RuntimeException("Order must be in status: PENDING_PAYMENT");
        }

        CreatePaymentResponseDto response = paymentHttpClient.createPayment(
            CreatePaymentRequestDto.builder()
                .orderId(id)
                .paymentMethod(request.paymentMethod())
                .amount(entity.getTotalAmount())
                .build()
        );

        var status = response.paymentStatus().equals(PaymentStatus.PAYMENT_SUCCEEDED)
                ? OrderStatus.PAID
                : OrderStatus.PAYMENT_FAILED;

        entity.setOrderStatus(status);

        if (status.equals(OrderStatus.PAID)) {
            sendOrderPaidEvent(entity, response);           // to kafka
        }

        return repository.save(entity);
    }


    private void sendOrderPaidEvent(
        OrderEntity entity,
        CreatePaymentResponseDto paymentResponseDto
    ) {
        kafkaTemplate.send(
            orderPaidTopic,
            entity.getId(),
            OrderPaidEvent.builder()
                .orderId(entity.getId())
                .amount(entity.getTotalAmount())
                .paymentMethod(paymentResponseDto.paymentMethod())
                .paymentId(paymentResponseDto.paymentId())
                .build()
        ).thenAccept(result -> {
            log.info("Order Paid event sent: id={}", entity.getId());
        });
    }

    public void processDeliveryAssigned(DeliveryAssignedEvent event) {
        OrderEntity order = getOrderOrThrow(event.orderId());

        if (!order.getOrderStatus().equals(OrderStatus.PAID)) {
            processIncorrectDeliveryState(order);
            return;
        }

        order.setOrderStatus(OrderStatus.DELIVERY_ASSIGNED);
        order.setCourierName(event.courierName());
        order.setEtaMinutes(event.etaMinutes());
        repository.save(order);
        log.info("Order delivery assigned processed: orderId={}", order.getId());
    }

    private void processIncorrectDeliveryState(OrderEntity order) {
        if (order.getOrderStatus().equals(OrderStatus.DELIVERY_ASSIGNED)) {
            log.info("Order delivery already processed: id={}", order.getId());
        } else {
            log.error("Trying to assign delivery but order have incorrect state id={}", order.getId());
        }
    }

}

package dev.sorokin.orderservice.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.sorokin.api.http.order.CreateOrderRequestDto;
import dev.sorokin.api.http.order.OrderDto;
import dev.sorokin.orderservice.domain.OrderProcessor;
import dev.sorokin.orderservice.domain.db.OrderEntity;
import dev.sorokin.orderservice.domain.db.OrderEntityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderProcessor orderProcessor;
    private final OrderEntityMapper orderEntityMapper;

    
    @PostMapping
    public OrderDto create(
        @RequestBody CreateOrderRequestDto request
    ) {
        log.info("Creating order: request={}", request);
        OrderEntity saved = orderProcessor.create(request);
        return orderEntityMapper.toOrderDto(saved); 
    }


    @GetMapping("/{id}")
    public OrderDto getOne(@PathVariable Long id) {
        log.info("Retrieving order with id {}", id);
        var found = orderProcessor.getOrderOrThrow(id);
        return orderEntityMapper.toOrderDto(found); 
    }


    @PostMapping("/{id}/pay")
    public OrderDto payOrder(
        @PathVariable Long id,
        @RequestBody OrderPaymentRequest request
    ) {
        log.info("Paying order with id={}, request={}", id, request);
        OrderEntity entity = orderProcessor.processPayment(id, request);
        return orderEntityMapper.toOrderDto(entity); 
    }
}

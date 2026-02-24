package dev.sorokin.orderservice.api;

import java.math.BigDecimal;
import java.util.Set;

import dev.sorokin.orderservice.domain.OrderStatus;

public record OrderDto(
    Long id,
    Long customerId,
    String address,
    BigDecimal totalAmount,
    String courierName,
    Integer etaMinutes,
    OrderStatus orderStatus,
    Set<OrderItemDto> items
) {

}

package dev.sorokin.api.http.order;

import java.util.Set;


public record CreateOrderRequestDto(
    Long customerId,
    String address,
    Set<OrderItemRequestDto> items
) {

}

package dev.sorokin.orderservice.api;

public record OrderItemRequestDto(
    Long itemId,
    Integer quantity,
    String name
) {

}

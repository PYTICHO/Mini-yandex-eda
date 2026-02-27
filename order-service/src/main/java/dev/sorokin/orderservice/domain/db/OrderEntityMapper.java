package dev.sorokin.orderservice.domain.db;

import org.mapstruct.*;

import dev.sorokin.api.http.order.CreateOrderRequestDto;
import dev.sorokin.api.http.order.OrderDto;


@Mapper(
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING
)
public interface OrderEntityMapper {

    OrderEntity toEntity(CreateOrderRequestDto requestDto);

    @AfterMapping
    default void linkOrderItemEntities(@MappingTarget OrderEntity orderEntity) {
        orderEntity.getItems().forEach(orderItemEntity -> orderItemEntity.setOrder(orderEntity));
    }

    OrderDto toOrderDto(OrderEntity orderEntity);
}
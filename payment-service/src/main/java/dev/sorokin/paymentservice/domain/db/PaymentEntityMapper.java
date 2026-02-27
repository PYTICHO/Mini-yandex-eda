package dev.sorokin.paymentservice.domain.db;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import dev.sorokin.api.http.payment.CreatePaymentRequestDto;
import dev.sorokin.api.http.payment.CreatePaymentResponseDto;

@Mapper(
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    componentModel = MappingConstants.ComponentModel.SPRING
)
public interface PaymentEntityMapper {

    public PaymentEntity toEntity(CreatePaymentRequestDto request);

    @Mapping(source = "id", target = "paymentId") // Так как названия полей разные, вно указываем какую связку маппить PaymentMethod - CreatePaymentResponseDto
    public CreatePaymentResponseDto toResponseDto(PaymentEntity entity);

}

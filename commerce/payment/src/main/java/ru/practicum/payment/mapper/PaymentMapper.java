package ru.practicum.payment.mapper;

import org.mapstruct.*;
import ru.practicum.interaction.api.payment.dto.PaymentDto;
import ru.practicum.payment.model.Payment;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface PaymentMapper {

    PaymentDto toDto(Payment payment);

    Payment toEntity(PaymentDto dto);
}
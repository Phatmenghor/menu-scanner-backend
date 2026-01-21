package com.emenu.features.order.mapper;

import com.emenu.features.order.dto.request.PaymentCreateRequest;
import com.emenu.features.order.dto.response.PaymentResponse;
import com.emenu.features.order.dto.update.PaymentUpdateRequest;
import com.emenu.features.order.models.Payment;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.mapper.PaginationMapper;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", uses = {PaginationMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "business", ignore = true)
    @Mapping(target = "plan", ignore = true)
    @Mapping(target = "subscription", ignore = true)
    @Mapping(target = "businessId", ignore = true)
    @Mapping(target = "planId", ignore = true)
    @Mapping(target = "subscriptionId", ignore = true)
    @Mapping(target = "amountKhr", ignore = true)
    @Mapping(target = "referenceNumber", ignore = true)
    Payment toEntity(PaymentCreateRequest request);

    @Mapping(source = "business.name", target = "businessName")
    @Mapping(source = "plan.name", target = "planName")
    @Mapping(source = "subscription.id", target = "subscriptionId")
    @Mapping(target = "subscriptionDisplayName", expression = "java(payment.getSubscriptionDisplayName())")
    @Mapping(target = "formattedAmount", expression = "java(payment.getFormattedAmount())")
    @Mapping(target = "formattedAmountKhr", expression = "java(payment.getFormattedAmountKhr())")
    PaymentResponse toResponse(Payment payment);

    List<PaymentResponse> toResponseList(List<Payment> payments);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "business", ignore = true)
    @Mapping(target = "plan", ignore = true)
    @Mapping(target = "subscription", ignore = true)
    @Mapping(target = "businessId", ignore = true)
    @Mapping(target = "planId", ignore = true)
    @Mapping(target = "subscriptionId", ignore = true)
    @Mapping(target = "amountKhr", ignore = true)
    void updateEntity(PaymentUpdateRequest request, @MappingTarget Payment payment);

    default PaginationResponse<PaymentResponse> toPaginationResponse(Page<Payment> paymentPage, PaginationMapper paginationMapper) {
        return paginationMapper.toPaginationResponse(paymentPage, this::toResponseList);
    }
}
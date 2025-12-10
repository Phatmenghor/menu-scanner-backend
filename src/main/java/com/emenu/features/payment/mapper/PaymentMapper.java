package com.emenu.features.payment.mapper;

import com.emenu.features.payment.dto.request.PaymentCreateRequest;
import com.emenu.features.payment.dto.response.PaymentResponse;
import com.emenu.features.payment.dto.update.PaymentUpdateRequest;
import com.emenu.features.payment.models.Payment;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.mapper.PaginationMapper;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class PaymentMapper {

    @Autowired
    protected PaginationMapper paginationMapper;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "business", ignore = true)
    @Mapping(target = "plan", ignore = true)
    @Mapping(target = "subscription", ignore = true)
    @Mapping(target = "amountKhr", ignore = true)
    public abstract Payment toEntity(PaymentCreateRequest request);

    @Mapping(source = "business.name", target = "businessName")
    @Mapping(source = "plan.name", target = "planName")
    @Mapping(source = "subscription.id", target = "subscriptionId")
    @Mapping(target = "subscriptionDisplayName", expression = "java(payment.getSubscriptionDisplayName())")
    @Mapping(target = "statusDescription", expression = "java(payment.getStatus().getDescription())")
    @Mapping(target = "formattedAmount", expression = "java(payment.getFormattedAmount())")
    @Mapping(target = "formattedAmountKhr", expression = "java(payment.getFormattedAmountKhr())")
    @Mapping(source = "imageUrl", target = "imageUrl")
    public abstract PaymentResponse toResponse(Payment payment);

    public abstract List<PaymentResponse> toResponseList(List<Payment> payments);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "business", ignore = true)
    @Mapping(target = "plan", ignore = true)
    @Mapping(target = "subscription", ignore = true)
    @Mapping(target = "amountKhr", ignore = true)
    public abstract void updateEntity(PaymentUpdateRequest request, @MappingTarget Payment payment);

    public PaginationResponse<PaymentResponse> toPaginationResponse(Page<Payment> paymentPage) {
        return paginationMapper.toPaginationResponse(paymentPage, this::toResponseList);
    }
}
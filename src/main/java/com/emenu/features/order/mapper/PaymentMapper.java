package com.emenu.features.order.mapper;

import com.emenu.enums.payment.PaymentMethod;
import com.emenu.enums.payment.PaymentStatus;
import com.emenu.enums.payment.PaymentType;
import com.emenu.features.order.dto.helper.PaymentCreateHelper;
import com.emenu.features.order.dto.request.PaymentCreateRequest;
import com.emenu.features.order.dto.response.PaymentResponse;
import com.emenu.features.order.dto.update.PaymentUpdateRequest;
import com.emenu.features.order.models.Payment;
import com.emenu.features.subscription.dto.request.SubscriptionCancelRequest;
import com.emenu.features.subscription.dto.request.SubscriptionRenewRequest;
import com.emenu.features.subscription.models.Subscription;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.mapper.PaginationMapper;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring", uses = {PaginationMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentMapper {

    @Mapping(target = "plan", ignore = true)
    @Mapping(target = "subscription", ignore = true)
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
    @Mapping(target = "plan", ignore = true)
    @Mapping(target = "subscription", ignore = true)
    @Mapping(target = "planId", ignore = true)
    @Mapping(target = "subscriptionId", ignore = true)
    @Mapping(target = "amountKhr", ignore = true)
    void updateEntity(PaymentUpdateRequest request, @MappingTarget Payment payment);

    default PaginationResponse<PaymentResponse> toPaginationResponse(Page<Payment> paymentPage, PaginationMapper paginationMapper) {
return paginationMapper.toPaginationResponse(paymentPage, this::toResponseList);
    }

    /**
     * Create payment from helper DTO - pure MapStruct mapping
     */
    Payment createFromHelper(PaymentCreateHelper helper);

    /**
     * Helper method to build PaymentCreateHelper for subscription renewal
     */
    default PaymentCreateHelper buildSubscriptionPaymentHelper(Subscription subscription, SubscriptionRenewRequest request) {
        return PaymentCreateHelper.builder()
                .businessId(subscription.getBusinessId())
                .planId(subscription.getPlanId())
                .subscriptionId(subscription.getId())
                .amount(request.getPaymentAmount())
                .paymentMethod(request.getPaymentMethod())
                .paymentType(PaymentType.SUBSCRIPTION)
                .status(PaymentStatus.COMPLETED)
                .notes("Payment for subscription renewal")
                .build();
    }

    /**
     * Helper method to build PaymentCreateHelper for subscription refund
     */
    default PaymentCreateHelper buildSubscriptionRefundHelper(Subscription subscription, SubscriptionCancelRequest request) {
        return PaymentCreateHelper.builder()
                .businessId(subscription.getBusinessId())
                .planId(subscription.getPlanId())
                .subscriptionId(subscription.getId())
                .amount(request.getRefundAmount().negate())
                .paymentMethod(PaymentMethod.OTHER)
                .paymentType(PaymentType.REFUND)
                .status(PaymentStatus.COMPLETED)
                .notes("Refund for cancelled subscription")
                .build();
    }

    /**
     * Helper method to build PaymentCreateHelper with subscription relationship
     */
    default PaymentCreateHelper buildPaymentHelper(UUID businessId, UUID planId, UUID subscriptionId,
                                                     BigDecimal amount, PaymentMethod method,
                                                     PaymentType type, String notes) {
        return PaymentCreateHelper.builder()
                .businessId(businessId)
                .planId(planId)
                .subscriptionId(subscriptionId)
                .amount(amount)
                .paymentMethod(method)
                .paymentType(type)
                .status(PaymentStatus.COMPLETED)
                .notes(notes)
                .build();
    }

    /**
     * Update payment with subscription relationship
     */
    @Mapping(source = "subscription.businessId", target = "businessId")
    @Mapping(source = "subscription.planId", target = "planId")
    @Mapping(source = "subscription.id", target = "subscriptionId")
    void updateWithSubscription(@MappingTarget Payment payment, Subscription subscription);
}
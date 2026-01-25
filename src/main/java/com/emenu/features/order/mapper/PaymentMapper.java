package com.emenu.features.order.mapper;

import com.emenu.enums.payment.PaymentMethod;
import com.emenu.enums.payment.PaymentStatus;
import com.emenu.enums.payment.PaymentType;
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
     * Create a payment for subscription renewal
     */
    default Payment createSubscriptionPayment(Subscription subscription, SubscriptionRenewRequest request) {
        Payment payment = new Payment();
        payment.setBusinessId(subscription.getBusinessId());
        payment.setPlanId(subscription.getPlanId());
        payment.setSubscriptionId(subscription.getId());
        payment.setAmount(request.getPaymentAmount());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setPaymentType(PaymentType.SUBSCRIPTION);
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setNotes("Payment for subscription renewal");
        return payment;
    }

    /**
     * Create a refund payment for subscription cancellation
     */
    default Payment createSubscriptionRefund(Subscription subscription, SubscriptionCancelRequest request) {
        Payment refund = new Payment();
        refund.setBusinessId(subscription.getBusinessId());
        refund.setPlanId(subscription.getPlanId());
        refund.setSubscriptionId(subscription.getId());
        refund.setAmount(request.getRefundAmount().negate());
        refund.setPaymentMethod(PaymentMethod.OTHER);
        refund.setPaymentType(PaymentType.REFUND);
        refund.setStatus(PaymentStatus.COMPLETED);
        refund.setNotes("Refund for cancelled subscription");
        return refund;
    }

    /**
     * Create a payment with subscription relationship
     */
    default Payment createPaymentForSubscription(UUID businessId, UUID planId, UUID subscriptionId,
                                                 BigDecimal amount, PaymentMethod method,
                                                 PaymentType type, String notes) {
        Payment payment = new Payment();
        payment.setBusinessId(businessId);
        payment.setPlanId(planId);
        payment.setSubscriptionId(subscriptionId);
        payment.setAmount(amount);
        payment.setPaymentMethod(method);
        payment.setPaymentType(type);
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setNotes(notes);
        return payment;
    }

    /**
     * Set subscription relationship on payment
     */
    default void setSubscriptionRelationship(Payment payment, Subscription subscription) {
        payment.setBusinessId(subscription.getBusinessId());
        payment.setPlanId(subscription.getPlanId());
        payment.setSubscriptionId(subscription.getId());
    }

    /**
     * Set subscription relationship on payment by IDs
     */
    default void setSubscriptionRelationship(Payment payment, UUID businessId, UUID planId, UUID subscriptionId) {
        payment.setBusinessId(businessId);
        payment.setPlanId(planId);
        payment.setSubscriptionId(subscriptionId);
    }
}
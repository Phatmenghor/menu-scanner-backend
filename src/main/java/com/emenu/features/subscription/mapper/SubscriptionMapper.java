package com.emenu.features.subscription.mapper;

import com.emenu.features.subscription.dto.request.SubscriptionCreateRequest;
import com.emenu.features.subscription.dto.response.SubscriptionResponse;
import com.emenu.features.subscription.dto.update.SubscriptionUpdateRequest;
import com.emenu.features.subscription.models.Subscription;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.mapper.PaginationMapper;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class SubscriptionMapper {

    @Autowired
    protected PaginationMapper paginationMapper;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "business", ignore = true)
    @Mapping(target = "plan", ignore = true)
    @Mapping(target = "payments", ignore = true)
    @Mapping(target = "startDate", ignore = true)
    @Mapping(target = "endDate", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    public abstract Subscription toEntity(SubscriptionCreateRequest request);

    @Mapping(source = "business.name", target = "businessName")
    @Mapping(source = "plan.name", target = "planName")
    @Mapping(source = "plan.price", target = "planPrice")
    @Mapping(source = "plan.durationDays", target = "planDurationDays")
    public abstract SubscriptionResponse toResponse(Subscription subscription);

    public abstract List<SubscriptionResponse> toResponseList(List<Subscription> subscriptions);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "businessId", ignore = true)
    @Mapping(target = "business", ignore = true)
    @Mapping(target = "plan", ignore = true)
    @Mapping(target = "payments", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    public abstract void updateEntity(SubscriptionUpdateRequest request, @MappingTarget Subscription subscription);

    @AfterMapping
    protected void setCalculatedFields(@MappingTarget SubscriptionResponse response, Subscription subscription) {
        response.setIsExpired(subscription.isExpired());
        response.setDaysRemaining(subscription.getDaysRemaining());
        response.setDisplayName(subscription.getDisplayName());
        
        // ✅ FIXED: Handle null business and plan
        if (subscription.getBusiness() != null) {
            response.setBusinessName(subscription.getBusiness().getName());
        } else {
            response.setBusinessName("Unknown Business");
        }
        
        if (subscription.getPlan() != null) {
            response.setPlanName(subscription.getPlan().getName());
            response.setPlanPrice(subscription.getPlan().getPrice().doubleValue());
            response.setPlanDurationDays(subscription.getPlan().getDurationDays());
        } else {
            response.setPlanName("Unknown Plan");
            response.setPlanPrice(0.0);
            response.setPlanDurationDays(0);
        }
        
        // ✅ FIXED: Set payment-related fields
        response.setTotalPaidAmount(subscription.getTotalPaidAmount());
        response.setIsFullyPaid(subscription.isFullyPaid());
        response.setPaymentStatusSummary(subscription.getPaymentStatusSummary());
        
        // Count payments if available
        if (subscription.getPayments() != null) {
            response.setTotalPaymentsCount((long) subscription.getPayments().size());
            response.setCompletedPaymentsCount(
                subscription.getPayments().stream()
                    .filter(payment -> payment.getStatus().isCompleted())
                    .count()
            );
            response.setPendingPaymentsCount(
                subscription.getPayments().stream()
                    .filter(payment -> payment.getStatus().isPending())
                    .count()
            );
        } else {
            response.setTotalPaymentsCount(0L);
            response.setCompletedPaymentsCount(0L);
            response.setPendingPaymentsCount(0L);
        }
    }

    public PaginationResponse<SubscriptionResponse> toPaginationResponse(Page<Subscription> subscriptionPage) {
        return paginationMapper.toPaginationResponse(subscriptionPage, this::toResponseList);
    }
}
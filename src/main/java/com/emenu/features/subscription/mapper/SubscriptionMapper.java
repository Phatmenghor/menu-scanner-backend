package com.emenu.features.subscription.mapper;

import com.emenu.features.subscription.dto.request.SubscriptionCreateRequest;
import com.emenu.features.subscription.dto.response.SubscriptionResponse;
import com.emenu.features.subscription.dto.update.SubscriptionUpdateRequest;
import com.emenu.features.subscription.models.Subscription;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.mapper.PaginationMapper;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
@Slf4j
public abstract class SubscriptionMapper {

    @Autowired
    protected PaginationMapper paginationMapper;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "business", ignore = true)
    @Mapping(target = "plan", ignore = true)
    @Mapping(target = "payments", ignore = true)
    @Mapping(target = "startDate", ignore = true)
    @Mapping(target = "endDate", ignore = true)
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
    public abstract void updateEntity(SubscriptionUpdateRequest request, @MappingTarget Subscription subscription);

    @AfterMapping
    protected void setCalculatedFields(@MappingTarget SubscriptionResponse response, Subscription subscription) {
        log.debug("🔍 Setting calculated fields for subscription: {}", subscription.getId());
        
        // ✅ ENHANCED: Set basic calculated fields
        response.setIsExpired(subscription.isExpired());
        response.setDaysRemaining(subscription.getDaysRemaining());
        response.setDisplayName(subscription.getDisplayName());
        
        // ✅ ENHANCED: Handle business information with better logging
        if (subscription.getBusiness() != null) {
            response.setBusinessName(subscription.getBusiness().getName());
            log.debug("✅ Business loaded: {}", subscription.getBusiness().getName());
        } else {
            response.setBusinessName("Unknown Business");
            log.warn("⚠️ Business is NULL for subscription: {}", subscription.getId());
            
            // ✅ DEBUGGING: Try to get business name from business ID if available
            if (subscription.getBusinessId() != null) {
                log.warn("🔍 Business ID exists: {} but business entity is not loaded", subscription.getBusinessId());
            }
        }
        
        // ✅ ENHANCED: Handle plan information with better logging
        if (subscription.getPlan() != null) {
            response.setPlanName(subscription.getPlan().getName());
            response.setPlanPrice(subscription.getPlan().getPrice().doubleValue());
            response.setPlanDurationDays(subscription.getPlan().getDurationDays());
            log.debug("✅ Plan loaded: {} (${}, {} days)", 
                    subscription.getPlan().getName(), 
                    subscription.getPlan().getPrice(), 
                    subscription.getPlan().getDurationDays());
        } else {
            response.setPlanName("Unknown Plan");
            response.setPlanPrice(0.0);
            response.setPlanDurationDays(0);
            log.warn("⚠️ Plan is NULL for subscription: {}", subscription.getId());
            
            // ✅ DEBUGGING: Try to get plan name from plan ID if available
            if (subscription.getPlanId() != null) {
                log.warn("🔍 Plan ID exists: {} but plan entity is not loaded", subscription.getPlanId());
            }
        }
        
        // ✅ ENHANCED: Set payment-related fields with better handling
        try {
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
                log.debug("✅ Payment info: {} total, {} completed, {} pending", 
                        response.getTotalPaymentsCount(),
                        response.getCompletedPaymentsCount(),
                        response.getPendingPaymentsCount());
            } else {
                response.setTotalPaymentsCount(0L);
                response.setCompletedPaymentsCount(0L);
                response.setPendingPaymentsCount(0L);
                log.debug("⚠️ No payments loaded for subscription: {}", subscription.getId());
            }
        } catch (Exception e) {
            log.error("❌ Error setting payment fields for subscription {}: {}", subscription.getId(), e.getMessage());
            // Set default values on error
            response.setTotalPaidAmount(java.math.BigDecimal.ZERO);
            response.setIsFullyPaid(false);
            response.setPaymentStatusSummary("Error loading payment info");
            response.setTotalPaymentsCount(0L);
            response.setCompletedPaymentsCount(0L);
            response.setPendingPaymentsCount(0L);
        }
        
        log.debug("✅ Calculated fields set for subscription: {} - Business: {}, Plan: {}", 
                subscription.getId(), response.getBusinessName(), response.getPlanName());
    }

    public PaginationResponse<SubscriptionResponse> toPaginationResponse(Page<Subscription> subscriptionPage) {
        return paginationMapper.toPaginationResponse(subscriptionPage, this::toResponseList);
    }
}
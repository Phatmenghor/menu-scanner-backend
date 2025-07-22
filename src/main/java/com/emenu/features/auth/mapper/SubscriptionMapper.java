package com.emenu.features.auth.mapper;

import com.emenu.features.auth.dto.request.SubscriptionCreateRequest;
import com.emenu.features.auth.dto.response.SubscriptionResponse;
import com.emenu.features.auth.dto.update.SubscriptionUpdateRequest;
import com.emenu.features.auth.models.Subscription;
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
    @Mapping(source = "plan.name", target = "planDisplayName") // ✅ FIXED: Use plan.name instead of plan.displayName
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
    @Mapping(target = "startDate", ignore = true)
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
        response.setDisplayName(getSubscriptionDisplayName(subscription)); // ✅ FIXED: Create custom display name
        response.setHasCustomLimits(subscription.hasCustomLimits());
        
        response.setEffectiveMaxStaff(subscription.getEffectiveMaxStaff());
        response.setEffectiveMaxMenuItems(subscription.getEffectiveMaxMenuItems());
        response.setEffectiveMaxTables(subscription.getEffectiveMaxTables());
        response.setEffectiveDurationDays(subscription.getEffectiveDurationDays());
        
        response.setCanAddStaff(subscription.canAddStaff(0));
        response.setCanAddMenuItem(subscription.canAddMenuItem(0));
        response.setCanAddTable(subscription.canAddTable(0));
    }

    // ✅ FIXED: Custom method to create display name from plan name
    protected String getSubscriptionDisplayName(Subscription subscription) {
        if (subscription.getPlan() != null) {
            String planName = subscription.getPlan().getName();
            if (Boolean.TRUE.equals(subscription.getIsTrial())) {
                planName += " (Trial)";
            }
            if (subscription.hasCustomLimits()) {
                planName += " (Custom)";
            }
            return planName;
        }
        return "Unknown Plan";
    }

    public PaginationResponse<SubscriptionResponse> toPaginationResponse(Page<Subscription> subscriptionPage) {
        return paginationMapper.toPaginationResponse(subscriptionPage, this::toResponseList);
    }
}
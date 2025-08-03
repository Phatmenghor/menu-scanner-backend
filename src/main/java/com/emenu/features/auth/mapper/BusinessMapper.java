package com.emenu.features.auth.mapper;

import com.emenu.features.auth.dto.request.BusinessCreateRequest;
import com.emenu.features.auth.dto.response.BusinessResponse;
import com.emenu.features.auth.dto.update.BusinessUpdateRequest;
import com.emenu.features.auth.models.Business;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.mapper.PaginationMapper;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class BusinessMapper {

    @Autowired
    protected PaginationMapper paginationMapper;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "users", ignore = true)
    @Mapping(target = "subscriptions", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "logoUrl", ignore = true)
    @Mapping(target = "website", ignore = true)
    @Mapping(target = "businessType", ignore = true)
    @Mapping(target = "cuisineType", ignore = true)
    @Mapping(target = "operatingHours", ignore = true)
    @Mapping(target = "facebookUrl", ignore = true)
    @Mapping(target = "instagramUrl", ignore = true)
    @Mapping(target = "telegramContact", ignore = true)
    @Mapping(target = "usdToKhrRate", constant = "4000.0")
    @Mapping(target = "currency", constant = "USD")
    @Mapping(target = "timezone", constant = "Asia/Phnom_Penh")
    @Mapping(target = "taxRate", constant = "0.0")
    @Mapping(target = "serviceChargeRate", constant = "0.0")
    @Mapping(target = "acceptsOnlinePayment", constant = "false")
    @Mapping(target = "acceptsCashPayment", constant = "true")
    @Mapping(target = "acceptsBankTransfer", constant = "false")
    @Mapping(target = "acceptsMobilePayment", constant = "false")
    @Mapping(target = "subscriptionStartDate", ignore = true)
    @Mapping(target = "subscriptionEndDate", ignore = true)
    @Mapping(target = "isSubscriptionActive", constant = "false")
    public abstract Business toEntity(BusinessCreateRequest request);

    public abstract BusinessResponse toResponse(Business business);
    public abstract List<BusinessResponse> toResponseList(List<Business> businesses);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "users", ignore = true)
    @Mapping(target = "subscriptions", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "logoUrl", ignore = true)
    @Mapping(target = "website", ignore = true)
    @Mapping(target = "businessType", ignore = true)
    @Mapping(target = "cuisineType", ignore = true)
    @Mapping(target = "operatingHours", ignore = true)
    @Mapping(target = "facebookUrl", ignore = true)
    @Mapping(target = "instagramUrl", ignore = true)
    @Mapping(target = "telegramContact", ignore = true)
    @Mapping(target = "usdToKhrRate", ignore = true)
    @Mapping(target = "currency", ignore = true)
    @Mapping(target = "timezone", ignore = true)
    @Mapping(target = "taxRate", ignore = true)
    @Mapping(target = "serviceChargeRate", ignore = true)
    @Mapping(target = "acceptsOnlinePayment", ignore = true)
    @Mapping(target = "acceptsCashPayment", ignore = true)
    @Mapping(target = "acceptsBankTransfer", ignore = true)
    @Mapping(target = "acceptsMobilePayment", ignore = true)
    @Mapping(target = "subscriptionStartDate", ignore = true)
    @Mapping(target = "subscriptionEndDate", ignore = true)
    @Mapping(target = "isSubscriptionActive", ignore = true)
    public abstract void updateEntity(BusinessUpdateRequest request, @MappingTarget Business business);

    @AfterMapping
    protected void setCalculatedFields(@MappingTarget BusinessResponse response, Business business) {
        // ✅ FIXED: Calculate subscription status properly
        boolean hasActiveSubscription = business.hasActiveSubscription();
        response.setHasActiveSubscription(hasActiveSubscription);
        response.setIsSubscriptionActive(hasActiveSubscription);
        response.setIsExpiringSoon(business.isSubscriptionExpiringSoon(7));
        response.setDaysRemaining(business.getDaysRemaining());
        
        // ✅ FIXED: Get current subscription plan from active subscriptions
        if (hasActiveSubscription && business.getSubscriptions() != null) {
            business.getSubscriptions().stream()
                    .filter(sub -> sub.getIsActive() && !sub.isExpired())
                    .findFirst()
                    .ifPresent(subscription -> {
                        if (subscription.getPlan() != null) {
                            response.setCurrentSubscriptionPlan(subscription.getPlan().getName());
                        } else {
                            response.setCurrentSubscriptionPlan("Unknown Plan");
                        }
                    });
        } else {
            response.setCurrentSubscriptionPlan(null);
        }
        
        if (response.getCurrentSubscriptionPlan() == null) {
            response.setCurrentSubscriptionPlan("No Active Plan");
        }
    }

    // ✅ UNIVERSAL PAGINATION MAPPER USAGE
    public PaginationResponse<BusinessResponse> toPaginationResponse(Page<Business> businessPage) {
        return paginationMapper.toPaginationResponse(businessPage, this::toResponseList);
    }
}
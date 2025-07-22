package com.emenu.features.auth.mapper;

import com.emenu.features.auth.dto.request.BusinessSettingsRequest;
import com.emenu.features.auth.dto.response.BusinessSettingsResponse;
import com.emenu.features.auth.models.Business;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class BusinessSettingsMapper {

    // Map Business entity to BusinessSettingsResponse
    @Mapping(source = "id", target = "businessId")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "logoUrl", target = "logoUrl")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "phone", target = "phone")
    @Mapping(source = "address", target = "address")
    @Mapping(source = "website", target = "website")
    @Mapping(source = "businessType", target = "businessType")
    @Mapping(source = "cuisineType", target = "cuisineType")
    @Mapping(source = "operatingHours", target = "operatingHours")
    @Mapping(source = "facebookUrl", target = "facebookUrl")
    @Mapping(source = "instagramUrl", target = "instagramUrl")
    @Mapping(source = "telegramContact", target = "telegramContact")
    @Mapping(source = "usdToKhrRate", target = "usdToKhrRate")
    @Mapping(source = "taxRate", target = "taxRate")
    @Mapping(source = "serviceChargeRate", target = "serviceChargeRate")
    @Mapping(source = "acceptsOnlinePayment", target = "acceptsOnlinePayment")
    @Mapping(source = "acceptsCashPayment", target = "acceptsCashPayment")
    @Mapping(source = "acceptsBankTransfer", target = "acceptsBankTransfer")
    @Mapping(source = "acceptsMobilePayment", target = "acceptsMobilePayment")
    @Mapping(source = "currency", target = "currency")
    @Mapping(source = "timezone", target = "timezone")
    @Mapping(source = "updatedAt", target = "updatedAt")
    @Mapping(target = "hasActiveSubscription", expression = "java(business.hasActiveSubscription())")
    @Mapping(target = "daysRemaining", expression = "java(business.getDaysRemaining())")
    @Mapping(target = "subscriptionEndDate", source = "subscriptionEndDate")
    @Mapping(target = "currentPlan", ignore = true) // Will be set in @AfterMapping
    public abstract BusinessSettingsResponse toResponse(Business business);

    // Update Business entity from BusinessSettingsRequest
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true) // Email should not be updated via settings
    @Mapping(target = "status", ignore = true) // Status is managed separately
    @Mapping(target = "subscriptionStartDate", ignore = true)
    @Mapping(target = "subscriptionEndDate", ignore = true)
    @Mapping(target = "isSubscriptionActive", ignore = true)
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
    @Mapping(target = "currency", ignore = true) // Fixed for Cambodia
    @Mapping(target = "timezone", ignore = true) // Fixed for Cambodia
    public abstract void updateEntity(BusinessSettingsRequest request, @MappingTarget Business business);

    @AfterMapping
    protected void setCurrentPlan(@MappingTarget BusinessSettingsResponse response, Business business) {
        // Get current plan from active subscription
        if (business.hasActiveSubscription() && business.getSubscriptions() != null) {
            business.getSubscriptions().stream()
                    .filter(sub -> sub.getIsActive() && !sub.isExpired())
                    .findFirst()
                    .ifPresent(subscription -> {
                        if (subscription.getPlan() != null) {
                            response.setCurrentPlan(subscription.getPlan().getDisplayName());
                        }
                    });
        }
    }

    // Validation methods (optional - can be used in service)
    public boolean isValidExchangeRate(Double rate) {
        return rate != null && rate >= 1000.0 && rate <= 10000.0;
    }

    public boolean isValidTaxRate(Double rate) {
        return rate != null && rate >= 0.0 && rate <= 100.0;
    }

    public boolean isValidServiceChargeRate(Double rate) {
        return rate != null && rate >= 0.0 && rate <= 100.0;
    }
}
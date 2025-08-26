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
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "usdToKhrRate", constant = "4000.0")
    @Mapping(target = "currency", constant = "USD")
    @Mapping(target = "timezone", constant = "Asia/Phnom_Penh")
    @Mapping(target = "taxRate", constant = "0.0")
    @Mapping(target = "isSubscriptionActive", constant = "false")
    public abstract Business toEntity(BusinessCreateRequest request);

    public abstract BusinessResponse toResponse(Business business);
    public abstract List<BusinessResponse> toResponseList(List<Business> businesses);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "users", ignore = true)
    public abstract void updateEntity(BusinessUpdateRequest request, @MappingTarget Business business);

    @AfterMapping
    protected void setCalculatedFields(@MappingTarget BusinessResponse response, Business business) {
        boolean hasActiveSubscription = business.hasActiveSubscription();
        response.setIsSubscriptionActive(hasActiveSubscription);
        response.setIsExpiringSoon(business.isSubscriptionExpiringSoon(7));
        response.setDaysRemaining(business.getDaysRemaining());
        
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

    public PaginationResponse<BusinessResponse> toPaginationResponse(Page<Business> businessPage) {
        return paginationMapper.toPaginationResponse(businessPage, this::toResponseList);
    }
}
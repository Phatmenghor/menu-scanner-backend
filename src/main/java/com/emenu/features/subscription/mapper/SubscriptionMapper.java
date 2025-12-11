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
    public abstract Subscription toEntity(SubscriptionCreateRequest request);

    @Mapping(source = "businessId", target = "businessId")
    @Mapping(source = "planId", target = "planId")
    @Mapping(source = "startDate", target = "startDate")
    @Mapping(source = "endDate", target = "endDate")
    @Mapping(source = "autoRenew", target = "autoRenew")
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
        response.setDaysRemaining(subscription.getDaysRemaining());
        response.setStatus(subscription.getStatus());
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
        response.setPaymentStatus(subscription.getPaymentStatus());
        response.setPaymentAmount(subscription.getPaymentAmount().doubleValue());
    }

    public PaginationResponse<SubscriptionResponse> toPaginationResponse(Page<Subscription> subscriptionPage) {
        return paginationMapper.toPaginationResponse(subscriptionPage, this::toResponseList);
    }
}
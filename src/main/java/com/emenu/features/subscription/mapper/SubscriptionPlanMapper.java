package com.emenu.features.subscription.mapper;

import com.emenu.features.subscription.dto.request.SubscriptionPlanCreateRequest;
import com.emenu.features.subscription.dto.response.SubscriptionPlanResponse;
import com.emenu.features.subscription.dto.update.SubscriptionPlanUpdateRequest;
import com.emenu.features.subscription.models.SubscriptionPlan;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.mapper.PaginationMapper;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class SubscriptionPlanMapper {

    @Autowired
    protected PaginationMapper paginationMapper;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "subscriptions", ignore = true)
    public abstract SubscriptionPlan toEntity(SubscriptionPlanCreateRequest request);

    public abstract SubscriptionPlanResponse toResponse(SubscriptionPlan plan);
    public abstract List<SubscriptionPlanResponse> toResponseList(List<SubscriptionPlan> plans);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "subscriptions", ignore = true)
    public abstract void updateEntity(SubscriptionPlanUpdateRequest request, @MappingTarget SubscriptionPlan plan);

    @AfterMapping
    protected void setCalculatedFields(@MappingTarget SubscriptionPlanResponse response, SubscriptionPlan plan) {
        
        if (plan.getSubscriptions() != null) {
            response.setActiveSubscriptionsCount((long) plan.getSubscriptions().size());
        } else {
            response.setActiveSubscriptionsCount(0L);
        }
    }

    public PaginationResponse<SubscriptionPlanResponse> toPaginationResponse(Page<SubscriptionPlan> planPage) {
        return paginationMapper.toPaginationResponse(planPage, this::toResponseList);
    }
}
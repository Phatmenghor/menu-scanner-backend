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

@Mapper(componentModel = "spring", uses = {PaginationMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SubscriptionPlanMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "subscriptions", ignore = true)
    SubscriptionPlan toEntity(SubscriptionPlanCreateRequest request);

    SubscriptionPlanResponse toResponse(SubscriptionPlan plan);
    List<SubscriptionPlanResponse> toResponseList(List<SubscriptionPlan> plans);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "subscriptions", ignore = true)
    void updateEntity(SubscriptionPlanUpdateRequest request, @MappingTarget SubscriptionPlan plan);

    @AfterMapping
    default void setCalculatedFields(@MappingTarget SubscriptionPlanResponse response, SubscriptionPlan plan) {
        if (plan.getSubscriptions() != null) {
            response.setActiveSubscriptionsCount((long) plan.getSubscriptions().size());
        } else {
            response.setActiveSubscriptionsCount(0L);
        }
    }

    default PaginationResponse<SubscriptionPlanResponse> toPaginationResponse(Page<SubscriptionPlan> planPage, PaginationMapper paginationMapper) {
        return paginationMapper.toPaginationResponse(planPage, this::toResponseList);
    }
}
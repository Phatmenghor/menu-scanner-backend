package com.emenu.features.services.mapper;

import com.emenu.enums.SubscriptionPlan;
import com.emenu.features.services.dto.request.CreatePlanRequest;
import com.emenu.features.services.dto.request.UpdatePlanRequest;
import com.emenu.features.services.dto.response.PlanResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SubscriptionPlanMapper {
    SubscriptionPlan toEntity(CreatePlanRequest request);
    PlanResponse toResponse(SubscriptionPlan plan);
    void updateEntity(UpdatePlanRequest request, @MappingTarget SubscriptionPlan plan);
}

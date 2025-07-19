package com.emenu.features.services.service;

import com.emenu.features.services.dto.request.CreatePlanRequest;
import com.emenu.features.services.dto.request.UpdatePlanRequest;
import com.emenu.features.services.dto.response.PlanResponse;

import java.util.List;
import java.util.UUID;

public interface SubscriptionPlanService {
    PlanResponse createPlan(CreatePlanRequest request);
    PlanResponse getPlan(UUID id);
    PlanResponse updatePlan(UUID id, UpdatePlanRequest request);
    void deletePlan(UUID id);
    void activatePlan(UUID id);
    void deactivatePlan(UUID id);
    List<PlanResponse> listPlans(Boolean activeOnly);
}
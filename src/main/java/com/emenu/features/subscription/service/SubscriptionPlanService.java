package com.emenu.features.subscription.service;

import com.emenu.features.subscription.dto.filter.SubscriptionPlanFilterRequest;
import com.emenu.features.subscription.dto.request.SubscriptionPlanCreateRequest;
import com.emenu.features.subscription.dto.response.SubscriptionPlanResponse;
import com.emenu.features.subscription.dto.update.SubscriptionPlanUpdateRequest;
import com.emenu.shared.dto.PaginationResponse;

import java.util.UUID;

public interface SubscriptionPlanService {

    // Plan Management with Filtering
    SubscriptionPlanResponse createPlan(SubscriptionPlanCreateRequest request);
    PaginationResponse<SubscriptionPlanResponse> getAllPlans(SubscriptionPlanFilterRequest filter);
    SubscriptionPlanResponse getPlanById(UUID planId);
    SubscriptionPlanResponse updatePlan(UUID planId, SubscriptionPlanUpdateRequest request);
    void deletePlan(UUID planId);
}
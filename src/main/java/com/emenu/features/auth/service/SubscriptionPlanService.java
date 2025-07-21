package com.emenu.features.auth.service;

import com.emenu.features.auth.dto.request.SubscriptionPlanCreateRequest;
import com.emenu.features.auth.dto.response.SubscriptionPlanResponse;
import com.emenu.features.auth.dto.update.SubscriptionPlanUpdateRequest;
import com.emenu.shared.dto.PaginationResponse;

import java.util.List;
import java.util.UUID;

public interface SubscriptionPlanService {

    // Plan Management
    SubscriptionPlanResponse createPlan(SubscriptionPlanCreateRequest request);
    List<SubscriptionPlanResponse> getAllPlans();
    PaginationResponse<SubscriptionPlanResponse> getAllPlans(int pageNo, int pageSize);
    List<SubscriptionPlanResponse> getAllActivePlans();
    List<SubscriptionPlanResponse> getPublicPlans();
    SubscriptionPlanResponse getPlanById(UUID planId);
    SubscriptionPlanResponse getPlanByName(String planName);
    SubscriptionPlanResponse updatePlan(UUID planId, SubscriptionPlanUpdateRequest request);
    void deletePlan(UUID planId);

    // Custom Plans
    SubscriptionPlanResponse createCustomPlan(UUID businessId, SubscriptionPlanCreateRequest request);
    List<SubscriptionPlanResponse> getCustomPlansForBusiness(UUID businessId);

    // Plan Operations
    void activatePlan(UUID planId);
    void deactivatePlan(UUID planId);
    SubscriptionPlanResponse setAsDefault(UUID planId);

    // System Operations
    void seedDefaultPlans();
    void updateDefaultPlans();

    // Plan Validation
    boolean canDeletePlan(UUID planId);
    boolean isPlanInUse(UUID planId);

    // Plan Statistics
    long getActiveSubscriptionsCount(UUID planId);
    long getTotalPlansCount();
}
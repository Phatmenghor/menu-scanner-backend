package com.emenu.features.auth.service;

import com.emenu.features.auth.dto.filter.SubscriptionPlanFilterRequest;
import com.emenu.features.auth.dto.request.SubscriptionPlanCreateRequest;
import com.emenu.features.auth.dto.response.SubscriptionPlanResponse;
import com.emenu.features.auth.dto.update.SubscriptionPlanUpdateRequest;
import com.emenu.shared.dto.PaginationResponse;

import java.util.List;
import java.util.UUID;

public interface SubscriptionPlanService {

    // Plan Management with Filtering
    SubscriptionPlanResponse createPlan(SubscriptionPlanCreateRequest request);
    PaginationResponse<SubscriptionPlanResponse> getAllPlans(SubscriptionPlanFilterRequest filter);
    List<SubscriptionPlanResponse> getPublicPlans();
    SubscriptionPlanResponse getPlanById(UUID planId);
    SubscriptionPlanResponse getPlanByName(String planName);
    SubscriptionPlanResponse updatePlan(UUID planId, SubscriptionPlanUpdateRequest request);
    void deletePlan(UUID planId);

    // Custom Plans Management
    SubscriptionPlanResponse createCustomPlan(UUID businessId, SubscriptionPlanCreateRequest request);
    List<SubscriptionPlanResponse> getCustomPlansForBusiness(UUID businessId);

    // Business Assignment Operations
    SubscriptionPlanResponse assignPlanToBusiness(UUID planId, UUID businessId, Boolean autoRenew, Integer customDurationDays);
    List<SubscriptionPlanResponse> bulkAssignPlan(UUID planId, List<UUID> businessIds, Boolean autoRenew);
    void unassignPlanFromBusiness(UUID planId, UUID businessId);

    // Plan Statistics & Analytics
    Object getPlanStatistics(UUID planId);
    long getActiveSubscriptionsCount(UUID planId);
    long getTotalPlansCount();
    Object getPlatformStatistics();

    // System Operations
    void seedDefaultPlans();
    void updateDefaultPlans();

    // Plan Validation
    boolean canDeletePlan(UUID planId);
    boolean isPlanInUse(UUID planId);
    boolean canBusinessUsePlan(UUID businessId, UUID planId);

    // Plan Recommendations
    List<SubscriptionPlanResponse> getRecommendedPlans(UUID businessId);
    List<SubscriptionPlanResponse> getSimilarPlans(UUID planId);

    // Plan Comparison
    Object comparePlans(List<UUID> planIds);
}
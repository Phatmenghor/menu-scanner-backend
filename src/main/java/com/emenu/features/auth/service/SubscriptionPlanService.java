package com.emenu.features.auth.service;

import com.emenu.features.auth.dto.request.SubscriptionPlanCreateRequest;
import com.emenu.features.auth.dto.response.SubscriptionPlanResponse;
import com.emenu.features.auth.dto.update.SubscriptionPlanUpdateRequest;
import com.emenu.shared.dto.PaginationResponse;

import java.util.List;
import java.util.UUID;

public interface SubscriptionPlanService {
    
    // CRUD Operations
    SubscriptionPlanResponse createPlan(SubscriptionPlanCreateRequest request);
    PaginationResponse<SubscriptionPlanResponse> getAllPlans(int pageNo, int pageSize);
    List<SubscriptionPlanResponse> getPublicPlans();
    List<SubscriptionPlanResponse> getAllActivePlans();
    SubscriptionPlanResponse getPlanById(UUID id);
    SubscriptionPlanResponse updatePlan(UUID id, SubscriptionPlanUpdateRequest request);
    void deletePlan(UUID id);
    
    // Plan Management
    void activatePlan(UUID id);
    void deactivatePlan(UUID id);
    SubscriptionPlanResponse setAsDefault(UUID id);
    
    // Custom Plans
    SubscriptionPlanResponse createCustomPlan(UUID businessId, SubscriptionPlanCreateRequest request);
    List<SubscriptionPlanResponse> getCustomPlansForBusiness(UUID businessId);
    
    // Utilities
    void seedDefaultPlans();
}
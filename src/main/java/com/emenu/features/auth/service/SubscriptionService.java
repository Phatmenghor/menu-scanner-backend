package com.emenu.features.auth.service;

import com.emenu.features.auth.dto.filter.SubscriptionFilterRequest;
import com.emenu.features.auth.dto.request.SubscriptionCreateRequest;
import com.emenu.features.auth.dto.response.SubscriptionResponse;
import com.emenu.features.auth.dto.update.SubscriptionUpdateRequest;
import com.emenu.shared.dto.PaginationResponse;

import java.util.List;
import java.util.UUID;

public interface SubscriptionService {
    
    // CRUD Operations
    SubscriptionResponse createSubscription(SubscriptionCreateRequest request);
    PaginationResponse<SubscriptionResponse> getSubscriptions(SubscriptionFilterRequest filter);
    SubscriptionResponse getSubscriptionById(UUID id);
    SubscriptionResponse updateSubscription(UUID id, SubscriptionUpdateRequest request);
    void deleteSubscription(UUID id);
    
    // Business Subscription Management
    SubscriptionResponse getActiveSubscriptionByBusiness(UUID businessId);
    List<SubscriptionResponse> getBusinessSubscriptionHistory(UUID businessId);
    
    // Subscription Operations
    SubscriptionResponse renewSubscription(UUID subscriptionId);
    void cancelSubscription(UUID subscriptionId);
    
    // Subscription Monitoring
    List<SubscriptionResponse> getExpiringSubscriptions(int days);
    List<SubscriptionResponse> getExpiredSubscriptions();
    void processExpiredSubscriptions();
}
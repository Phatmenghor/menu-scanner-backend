package com.emenu.features.subscription.service;

import com.emenu.features.subscription.dto.request.SubscriptionCreateRequest;
import com.emenu.features.subscription.dto.resposne.SubscriptionResponse;
import com.emenu.shared.dto.PaginationResponse;

import java.util.UUID;

public interface SubscriptionService {
    
    SubscriptionResponse createSubscription(UUID businessId, SubscriptionCreateRequest request);
    SubscriptionResponse getSubscriptionById(UUID id);
    SubscriptionResponse getSubscriptionByBusinessId(UUID businessId);
    PaginationResponse<SubscriptionResponse> getAllSubscriptions(int pageNo, int pageSize);
    SubscriptionResponse updateSubscription(UUID id, SubscriptionCreateRequest request);
    void cancelSubscription(UUID id);
    void renewSubscription(UUID id);
    
    // Business usage tracking
    void updateStaffUsage(UUID businessId, int count);
    void updateMenuItemUsage(UUID businessId, int count);
    void updateTableUsage(UUID businessId, int count);
    
    // Validation
    boolean canAddStaff(UUID businessId);
    boolean canAddMenuItem(UUID businessId);
    boolean canAddTable(UUID businessId);
    
    // Background tasks
    void processExpiredSubscriptions();
    void sendExpirationNotifications();
}

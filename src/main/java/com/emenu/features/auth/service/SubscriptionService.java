package com.emenu.features.auth.service;

import com.emenu.features.auth.dto.filter.SubscriptionFilterRequest;
import com.emenu.features.auth.dto.request.SubscriptionCreateRequest;
import com.emenu.features.auth.dto.response.SubscriptionResponse;
import com.emenu.features.auth.dto.update.SubscriptionUpdateRequest;
import com.emenu.shared.dto.PaginationResponse;

import java.util.List;
import java.util.UUID;

public interface SubscriptionService {
    
    // Enhanced CRUD Operations with Filtering
    SubscriptionResponse createSubscription(SubscriptionCreateRequest request);
    PaginationResponse<SubscriptionResponse> getSubscriptions(SubscriptionFilterRequest filter);
    PaginationResponse<SubscriptionResponse> getCurrentUserBusinessSubscriptions(SubscriptionFilterRequest filter);
    SubscriptionResponse getSubscriptionById(UUID id);
    SubscriptionResponse updateSubscription(UUID id, SubscriptionUpdateRequest request);
    void deleteSubscription(UUID id);
    
    // Business Subscription Management
    SubscriptionResponse getActiveSubscriptionByBusiness(UUID businessId);
    SubscriptionResponse getCurrentUserActiveSubscription();
    List<SubscriptionResponse> getBusinessSubscriptionHistory(UUID businessId);
    
    // Subscription Operations
    SubscriptionResponse renewSubscription(UUID subscriptionId, UUID newPlanId, Integer customDurationDays);
    void cancelSubscription(UUID subscriptionId, Boolean immediate);
    SubscriptionResponse suspendSubscription(UUID subscriptionId, String reason);
    SubscriptionResponse reactivateSubscription(UUID subscriptionId);
    SubscriptionResponse extendSubscription(UUID subscriptionId, Integer days, String reason);
    SubscriptionResponse changeSubscriptionPlan(UUID subscriptionId, UUID newPlanId, Boolean immediate);
    
    // Subscription Monitoring & Analytics
    List<SubscriptionResponse> getExpiringSubscriptions(int days);
    List<SubscriptionResponse> getExpiredSubscriptions();
    Object processExpiredSubscriptions();
    Object getSubscriptionUsage(UUID subscriptionId);
    Object getBusinessSubscriptionAnalytics(UUID businessId);
    
    // Bulk Operations
    Object bulkOperations(String action, List<UUID> subscriptionIds, String reason);
    List<SubscriptionResponse> bulkRenewSubscriptions(List<UUID> subscriptionIds);
    List<SubscriptionResponse> bulkCancelSubscriptions(List<UUID> subscriptionIds, String reason);
    List<SubscriptionResponse> bulkSuspendSubscriptions(List<UUID> subscriptionIds, String reason);
    
    // Access Control
    boolean canAccessSubscription(UUID subscriptionId);
    boolean canModifySubscription(UUID subscriptionId);
    
    // Subscription Validation
    boolean isValidSubscriptionForBusiness(UUID businessId, UUID planId);
    boolean hasActiveSubscription(UUID businessId);
    boolean canUpgradeToPlan(UUID subscriptionId, UUID newPlanId);
    boolean canDowngradeToPlan(UUID subscriptionId, UUID newPlanId);
    
    // Automated Processes
    void sendExpiryNotifications();
    void processAutoRenewals();
    void updateSubscriptionStatuses();
}

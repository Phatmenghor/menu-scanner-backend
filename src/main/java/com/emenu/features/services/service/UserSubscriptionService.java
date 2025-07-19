package com.emenu.features.services.service;

import com.emenu.features.services.dto.filter.SubscriptionFilterRequest;
import com.emenu.features.services.dto.request.CreateSubscriptionRequest;
import com.emenu.features.services.dto.response.SubscriptionResponse;
import com.emenu.features.services.dto.update.UpdateSubscriptionRequest;
import com.emenu.shared.dto.PaginationResponse;

import java.util.UUID;

public interface UserSubscriptionService {
    SubscriptionResponse createSubscription(CreateSubscriptionRequest request);
    SubscriptionResponse getSubscription(UUID id);
    SubscriptionResponse updateSubscription(UUID id, UpdateSubscriptionRequest request);
    void cancelSubscription(UUID id);
    SubscriptionResponse renewSubscription(UUID id);
    SubscriptionResponse getUserSubscription(UUID userId);
    SubscriptionResponse getCurrentUserSubscription();
    PaginationResponse<SubscriptionResponse> listSubscriptions(SubscriptionFilterRequest filter);
    
    // Usage tracking methods
    boolean canCreateUser(UUID subscriptionId);
    boolean canCreateMenu(UUID subscriptionId);
    boolean canProcessOrder(UUID subscriptionId);
    void incrementUserCount(UUID subscriptionId);
    void incrementMenuCount(UUID subscriptionId);
    void incrementOrderCount(UUID subscriptionId);
}
package com.emenu.features.auth.service;

import com.emenu.features.auth.dto.filter.SubscriptionFilterRequest;
import com.emenu.features.auth.dto.request.SubscriptionCancelRequest;
import com.emenu.features.auth.dto.request.SubscriptionCreateRequest;
import com.emenu.features.auth.dto.response.SubscriptionResponse;
import com.emenu.features.auth.dto.update.SubscriptionRenewRequest;
import com.emenu.features.auth.dto.update.SubscriptionUpdateRequest;
import com.emenu.shared.dto.PaginationResponse;

import java.util.UUID;

public interface SubscriptionService {
    
    // Basic CRUD Operations
    SubscriptionResponse createSubscription(SubscriptionCreateRequest request);
    PaginationResponse<SubscriptionResponse> getSubscriptions(SubscriptionFilterRequest filter);
    PaginationResponse<SubscriptionResponse> getCurrentUserBusinessSubscriptions(SubscriptionFilterRequest filter);
    SubscriptionResponse getSubscriptionById(UUID id);
    SubscriptionResponse updateSubscription(UUID id, SubscriptionUpdateRequest request);
    SubscriptionResponse deleteSubscription(UUID id); // Now returns SubscriptionResponse
    
    // Operations with Request Bodies
    SubscriptionResponse renewSubscription(UUID subscriptionId, SubscriptionRenewRequest request);
    SubscriptionResponse cancelSubscription(UUID subscriptionId, SubscriptionCancelRequest request);
}
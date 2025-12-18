package com.emenu.features.auth.service;

import com.emenu.features.auth.dto.filter.BusinessOwnerFilterRequest;
import com.emenu.features.auth.dto.request.BusinessOwnerChangePlanRequest;
import com.emenu.features.auth.dto.request.BusinessOwnerCreateRequest;
import com.emenu.features.auth.dto.request.BusinessOwnerSubscriptionCancelRequest;
import com.emenu.features.auth.dto.request.BusinessOwnerSubscriptionRenewRequest;
import com.emenu.features.auth.dto.response.BusinessOwnerCreateResponse;
import com.emenu.features.auth.dto.response.BusinessOwnerDetailResponse;
import com.emenu.shared.dto.PaginationResponse;

import java.util.UUID;

public interface BusinessOwnerService {
    
    BusinessOwnerCreateResponse createBusinessOwner(BusinessOwnerCreateRequest request);
    
    PaginationResponse<BusinessOwnerDetailResponse> getAllBusinessOwners(BusinessOwnerFilterRequest request);
    
    BusinessOwnerDetailResponse getBusinessOwnerDetail(UUID ownerId);
    
    BusinessOwnerDetailResponse renewSubscription(UUID ownerId, BusinessOwnerSubscriptionRenewRequest request);
    
    BusinessOwnerDetailResponse changePlan(UUID ownerId, BusinessOwnerChangePlanRequest request);
    
    BusinessOwnerDetailResponse cancelSubscription(UUID ownerId, BusinessOwnerSubscriptionCancelRequest request);
    
    BusinessOwnerDetailResponse deleteBusinessOwner(UUID ownerId);
}



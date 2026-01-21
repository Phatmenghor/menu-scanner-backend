package com.emenu.features.order.service;

import com.emenu.features.order.dto.filter.BusinessExchangeRateFilterRequest;
import com.emenu.features.order.dto.request.BusinessExchangeRateCreateRequest;
import com.emenu.features.order.dto.response.BusinessExchangeRateResponse;
import com.emenu.features.order.dto.update.BusinessExchangeRateUpdateRequest;
import com.emenu.shared.dto.PaginationResponse;

import java.util.UUID;

public interface BusinessExchangeRateService {
    
    /**
     * Create new business exchange rate (deactivates previous active rate for the business)
     */
    BusinessExchangeRateResponse createBusinessExchangeRate(BusinessExchangeRateCreateRequest request);
    
    /**
     * Get all business exchange rates with filtering and pagination
     */
    PaginationResponse<BusinessExchangeRateResponse> getAllBusinessExchangeRates(BusinessExchangeRateFilterRequest filter);
    
    /**
     * Get business exchange rate by ID
     */
    BusinessExchangeRateResponse getBusinessExchangeRateById(UUID id);
    
    /**
     * Update business exchange rate
     */
    BusinessExchangeRateResponse updateBusinessExchangeRate(UUID id, BusinessExchangeRateUpdateRequest request);
    
    /**
     * Delete business exchange rate
     */
    BusinessExchangeRateResponse deleteBusinessExchangeRate(UUID id);
    
    /**
     * Get current active exchange rate for a business
     */
    BusinessExchangeRateResponse getActiveRateByBusinessId(UUID businessId);
}
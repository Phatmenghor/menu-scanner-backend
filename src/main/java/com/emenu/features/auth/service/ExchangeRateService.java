package com.emenu.features.auth.service;

import com.emenu.features.auth.dto.filter.ExchangeRateFilterRequest;
import com.emenu.features.auth.dto.request.ExchangeRateCreateRequest;
import com.emenu.features.auth.dto.response.ExchangeRateResponse;
import com.emenu.features.auth.dto.update.ExchangeRateUpdateRequest;
import com.emenu.shared.dto.PaginationResponse;

import java.util.List;
import java.util.UUID;

public interface ExchangeRateService {
    
    // Basic CRUD Operations
    ExchangeRateResponse createExchangeRate(ExchangeRateCreateRequest request);
    PaginationResponse<ExchangeRateResponse> getAllExchangeRates(ExchangeRateFilterRequest filter);
    ExchangeRateResponse getExchangeRateById(UUID id);
    ExchangeRateResponse updateExchangeRate(UUID id, ExchangeRateUpdateRequest request);
    ExchangeRateResponse deleteExchangeRate(UUID id);
    
    // System Default Operations
    ExchangeRateResponse createSystemDefault(Double rate, String notes);
    ExchangeRateResponse getActiveSystemDefault();
    ExchangeRateResponse updateSystemDefault(Double newRate, String notes);
    
    // Business Specific Operations
    ExchangeRateResponse createBusinessRate(UUID businessId, Double rate, String notes);
    ExchangeRateResponse getActiveBusinessRate(UUID businessId);
    List<ExchangeRateResponse> getBusinessRateHistory(UUID businessId);
    ExchangeRateResponse updateBusinessRate(UUID businessId, Double newRate, String notes);
    
    // Utility Operations
    Double getCurrentRate(UUID businessId); // Returns business rate or system default
    List<ExchangeRateResponse> getAllActiveRates();
    
    // Activation/Deactivation
    ExchangeRateResponse activateRate(UUID id);
    ExchangeRateResponse deactivateRate(UUID id);
    
    // Statistics
    long getTotalRatesCount();
    long getActiveRatesCount();
    long getBusinessesWithRatesCount();
}

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
    
    // System Rate Operations
    ExchangeRateResponse getCurrentActiveRate();
    Double getCurrentRateValue(); // Returns just the double value for calculations
}
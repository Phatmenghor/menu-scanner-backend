package com.emenu.features.payment.service;

import com.emenu.features.payment.dto.filter.ExchangeRateFilterRequest;
import com.emenu.features.payment.dto.request.ExchangeRateCreateRequest;
import com.emenu.features.payment.dto.response.ExchangeRateResponse;
import com.emenu.features.payment.dto.update.ExchangeRateUpdateRequest;
import com.emenu.shared.dto.PaginationResponse;

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
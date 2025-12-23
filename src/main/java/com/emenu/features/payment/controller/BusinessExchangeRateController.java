package com.emenu.features.payment.controller;

import com.emenu.features.payment.dto.filter.BusinessExchangeRateFilterRequest;
import com.emenu.features.payment.dto.request.BusinessExchangeRateCreateRequest;
import com.emenu.features.payment.dto.response.BusinessExchangeRateResponse;
import com.emenu.features.payment.dto.update.BusinessExchangeRateUpdateRequest;
import com.emenu.features.payment.service.BusinessExchangeRateService;
import com.emenu.shared.dto.ApiResponse;
import com.emenu.shared.dto.PaginationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/business-exchange-rates")
@RequiredArgsConstructor
@Slf4j
public class BusinessExchangeRateController {

    private final BusinessExchangeRateService exchangeRateService;

    /**
     * Create new business exchange rate (deactivates previous active rate)
     */
    @PostMapping
    public ResponseEntity<ApiResponse<BusinessExchangeRateResponse>> createBusinessExchangeRate(
            @Valid @RequestBody BusinessExchangeRateCreateRequest request) {
        log.info("POST /business-exchange-rates - businessId: {}, rate: {}", 
                request.getBusinessId(), request.getUsdToKhrRate());
        
        BusinessExchangeRateResponse exchangeRate = exchangeRateService.createBusinessExchangeRate(request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Business exchange rate created successfully", exchangeRate));
    }

    /**
     * Get all business exchange rates with filtering and pagination
     */
    @PostMapping("/all")
    public ResponseEntity<ApiResponse<PaginationResponse<BusinessExchangeRateResponse>>> getAllBusinessExchangeRates(
            @Valid @RequestBody BusinessExchangeRateFilterRequest filter) {
        log.info("POST /business-exchange-rates/all - page: {}", filter.getPageNo());
        
        PaginationResponse<BusinessExchangeRateResponse> exchangeRates = 
                exchangeRateService.getAllBusinessExchangeRates(filter);
        
        return ResponseEntity.ok(ApiResponse.success("Business exchange rates retrieved successfully", exchangeRates));
    }

    /**
     * Get business exchange rate by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BusinessExchangeRateResponse>> getBusinessExchangeRateById(
            @PathVariable UUID id) {
        log.info("GET /business-exchange-rates/{}", id);
        
        BusinessExchangeRateResponse exchangeRate = exchangeRateService.getBusinessExchangeRateById(id);
        
        return ResponseEntity.ok(ApiResponse.success("Business exchange rate retrieved successfully", exchangeRate));
    }

    /**
     * Update business exchange rate
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BusinessExchangeRateResponse>> updateBusinessExchangeRate(
            @PathVariable UUID id,
            @Valid @RequestBody BusinessExchangeRateUpdateRequest request) {
        log.info("PUT /business-exchange-rates/{}", id);
        
        BusinessExchangeRateResponse exchangeRate = exchangeRateService.updateBusinessExchangeRate(id, request);
        
        return ResponseEntity.ok(ApiResponse.success("Business exchange rate updated successfully", exchangeRate));
    }

    /**
     * Delete business exchange rate
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<BusinessExchangeRateResponse>> deleteBusinessExchangeRate(
            @PathVariable UUID id) {
        log.info("DELETE /business-exchange-rates/{}", id);
        
        BusinessExchangeRateResponse exchangeRate = exchangeRateService.deleteBusinessExchangeRate(id);
        
        return ResponseEntity.ok(ApiResponse.success("Business exchange rate deleted successfully", exchangeRate));
    }

    /**
     * Get current active exchange rate for a business
     */
    @GetMapping("/business/{businessId}/active")
    public ResponseEntity<ApiResponse<BusinessExchangeRateResponse>> getActiveRateByBusinessId(
            @PathVariable UUID businessId) {
        log.info("GET /business-exchange-rates/business/{}/active", businessId);

        BusinessExchangeRateResponse exchangeRate = exchangeRateService.getActiveRateByBusinessId(businessId);

        return ResponseEntity.ok(ApiResponse.success("Active business exchange rate retrieved successfully", exchangeRate));
    }
}
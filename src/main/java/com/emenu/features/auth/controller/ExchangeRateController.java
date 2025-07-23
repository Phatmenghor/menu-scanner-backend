package com.emenu.features.auth.controller;

import com.emenu.features.auth.dto.filter.ExchangeRateFilterRequest;
import com.emenu.features.auth.dto.request.ExchangeRateCreateRequest;
import com.emenu.features.auth.dto.response.ExchangeRateResponse;
import com.emenu.features.auth.dto.update.ExchangeRateUpdateRequest;
import com.emenu.features.auth.service.ExchangeRateService;
import com.emenu.shared.dto.ApiResponse;
import com.emenu.shared.dto.PaginationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/exchange-rates")
@RequiredArgsConstructor
@Slf4j
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    @PostMapping
    public ResponseEntity<ApiResponse<ExchangeRateResponse>> createExchangeRate(@Valid @RequestBody ExchangeRateCreateRequest request) {
        log.info("Creating exchange rate: {} for business: {}", request.getUsdToKhrRate(), request.getBusinessId());
        ExchangeRateResponse exchangeRate = exchangeRateService.createExchangeRate(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Exchange rate created successfully", exchangeRate));
    }

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<PaginationResponse<ExchangeRateResponse>>> getAllExchangeRates(@Valid @RequestBody ExchangeRateFilterRequest filter) {
        log.info("Getting all exchange rates with filter");
        PaginationResponse<ExchangeRateResponse> exchangeRates = exchangeRateService.getAllExchangeRates(filter);
        return ResponseEntity.ok(ApiResponse.success("Exchange rates retrieved successfully", exchangeRates));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ExchangeRateResponse>> getExchangeRateById(@PathVariable UUID id) {
        log.info("Getting exchange rate by ID: {}", id);
        ExchangeRateResponse exchangeRate = exchangeRateService.getExchangeRateById(id);
        return ResponseEntity.ok(ApiResponse.success("Exchange rate retrieved successfully", exchangeRate));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ExchangeRateResponse>> updateExchangeRate(@PathVariable UUID id, @Valid @RequestBody ExchangeRateUpdateRequest request) {
        log.info("Updating exchange rate: {}", id);
        ExchangeRateResponse exchangeRate = exchangeRateService.updateExchangeRate(id, request);
        return ResponseEntity.ok(ApiResponse.success("Exchange rate updated successfully", exchangeRate));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<ExchangeRateResponse>> deleteExchangeRate(@PathVariable UUID id) {
        log.info("Deleting exchange rate: {}", id);
        ExchangeRateResponse exchangeRate = exchangeRateService.deleteExchangeRate(id);
        return ResponseEntity.ok(ApiResponse.success("Exchange rate deleted successfully", exchangeRate));
    }

    // System Default Operations
    @PostMapping("/system-default")
    public ResponseEntity<ApiResponse<ExchangeRateResponse>> createSystemDefault(
            @RequestParam Double rate,
            @RequestParam(required = false) String notes) {
        log.info("Creating system default exchange rate: {}", rate);
        ExchangeRateResponse exchangeRate = exchangeRateService.createSystemDefault(rate, notes);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("System default exchange rate created successfully", exchangeRate));
    }

    @GetMapping("/system-default")
    public ResponseEntity<ApiResponse<ExchangeRateResponse>> getSystemDefault() {
        log.info("Getting system default exchange rate");
        ExchangeRateResponse exchangeRate = exchangeRateService.getActiveSystemDefault();
        return ResponseEntity.ok(ApiResponse.success("System default exchange rate retrieved successfully", exchangeRate));
    }

    @PutMapping("/system-default")
    public ResponseEntity<ApiResponse<ExchangeRateResponse>> updateSystemDefault(
            @RequestParam Double rate,
            @RequestParam(required = false) String notes) {
        log.info("Updating system default exchange rate to: {}", rate);
        ExchangeRateResponse exchangeRate = exchangeRateService.updateSystemDefault(rate, notes);
        return ResponseEntity.ok(ApiResponse.success("System default exchange rate updated successfully", exchangeRate));
    }

    // Business Specific Operations
    @PostMapping("/business/{businessId}")
    public ResponseEntity<ApiResponse<ExchangeRateResponse>> createBusinessRate(
            @PathVariable UUID businessId,
            @RequestParam Double rate,
            @RequestParam(required = false) String notes) {
        log.info("Creating business exchange rate: {} for business: {}", rate, businessId);
        ExchangeRateResponse exchangeRate = exchangeRateService.createBusinessRate(businessId, rate, notes);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Business exchange rate created successfully", exchangeRate));
    }

    @GetMapping("/business/{businessId}")
    public ResponseEntity<ApiResponse<ExchangeRateResponse>> getBusinessRate(@PathVariable UUID businessId) {
        log.info("Getting business exchange rate for: {}", businessId);
        ExchangeRateResponse exchangeRate = exchangeRateService.getActiveBusinessRate(businessId);
        return ResponseEntity.ok(ApiResponse.success("Business exchange rate retrieved successfully", exchangeRate));
    }

    @GetMapping("/business/{businessId}/history")
    public ResponseEntity<ApiResponse<List<ExchangeRateResponse>>> getBusinessRateHistory(@PathVariable UUID businessId) {
        log.info("Getting business exchange rate history for: {}", businessId);
        List<ExchangeRateResponse> exchangeRates = exchangeRateService.getBusinessRateHistory(businessId);
        return ResponseEntity.ok(ApiResponse.success("Business exchange rate history retrieved successfully", exchangeRates));
    }

    @PutMapping("/business/{businessId}")
    public ResponseEntity<ApiResponse<ExchangeRateResponse>> updateBusinessRate(
            @PathVariable UUID businessId,
            @RequestParam Double rate,
            @RequestParam(required = false) String notes) {
        log.info("Updating business exchange rate to: {} for business: {}", rate, businessId);
        ExchangeRateResponse exchangeRate = exchangeRateService.updateBusinessRate(businessId, rate, notes);
        return ResponseEntity.ok(ApiResponse.success("Business exchange rate updated successfully", exchangeRate));
    }

    // Utility Operations
    @GetMapping("/current")
    public ResponseEntity<ApiResponse<Double>> getCurrentRate(@RequestParam(required = false) UUID businessId) {
        log.info("Getting current exchange rate for business: {}", businessId);
        Double rate = exchangeRateService.getCurrentRate(businessId);
        return ResponseEntity.ok(ApiResponse.success("Current exchange rate retrieved successfully", rate));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<ExchangeRateResponse>>> getAllActiveRates() {
        log.info("Getting all active exchange rates");
        List<ExchangeRateResponse> exchangeRates = exchangeRateService.getAllActiveRates();
        return ResponseEntity.ok(ApiResponse.success("Active exchange rates retrieved successfully", exchangeRates));
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<ExchangeRateResponse>> activateRate(@PathVariable UUID id) {
        log.info("Activating exchange rate: {}", id);
        ExchangeRateResponse exchangeRate = exchangeRateService.activateRate(id);
        return ResponseEntity.ok(ApiResponse.success("Exchange rate activated successfully", exchangeRate));
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<ExchangeRateResponse>> deactivateRate(@PathVariable UUID id) {
        log.info("Deactivating exchange rate: {}", id);
        ExchangeRateResponse exchangeRate = exchangeRateService.deactivateRate(id);
        return ResponseEntity.ok(ApiResponse.success("Exchange rate deactivated successfully", exchangeRate));
    }

    // Statistics
    @GetMapping("/stats/total")
    public ResponseEntity<ApiResponse<Long>> getTotalRatesCount() {
        long count = exchangeRateService.getTotalRatesCount();
        return ResponseEntity.ok(ApiResponse.success("Total exchange rates count retrieved successfully", count));
    }

    @GetMapping("/stats/active")
    public ResponseEntity<ApiResponse<Long>> getActiveRatesCount() {
        long count = exchangeRateService.getActiveRatesCount();
        return ResponseEntity.ok(ApiResponse.success("Active exchange rates count retrieved successfully", count));
    }

    @GetMapping("/stats/businesses")
    public ResponseEntity<ApiResponse<Long>> getBusinessesWithRatesCount() {
        long count = exchangeRateService.getBusinessesWithRatesCount();
        return ResponseEntity.ok(ApiResponse.success("Businesses with rates count retrieved successfully", count));
    }
}
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

    /**
     * Create new system exchange rate (deactivates previous active rate)
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ExchangeRateResponse>> createExchangeRate(@Valid @RequestBody ExchangeRateCreateRequest request) {
        log.info("Creating system exchange rate: {}", request.getUsdToKhrRate());
        ExchangeRateResponse exchangeRate = exchangeRateService.createExchangeRate(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Exchange rate created successfully", exchangeRate));
    }

    /**
     * Get all exchange rates with filtering and pagination
     */
    @PostMapping("/all")
    public ResponseEntity<ApiResponse<PaginationResponse<ExchangeRateResponse>>> getAllExchangeRates(@Valid @RequestBody ExchangeRateFilterRequest filter) {
        log.info("Getting all exchange rates with filter");
        PaginationResponse<ExchangeRateResponse> exchangeRates = exchangeRateService.getAllExchangeRates(filter);
        return ResponseEntity.ok(ApiResponse.success("Exchange rates retrieved successfully", exchangeRates));
    }

    /**
     * Get exchange rate by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ExchangeRateResponse>> getExchangeRateById(@PathVariable UUID id) {
        log.info("Getting exchange rate by ID: {}", id);
        ExchangeRateResponse exchangeRate = exchangeRateService.getExchangeRateById(id);
        return ResponseEntity.ok(ApiResponse.success("Exchange rate retrieved successfully", exchangeRate));
    }

    /**
     * Update exchange rate
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ExchangeRateResponse>> updateExchangeRate(@PathVariable UUID id, @Valid @RequestBody ExchangeRateUpdateRequest request) {
        log.info("Updating exchange rate: {}", id);
        ExchangeRateResponse exchangeRate = exchangeRateService.updateExchangeRate(id, request);
        return ResponseEntity.ok(ApiResponse.success("Exchange rate updated successfully", exchangeRate));
    }

    /**
     * Delete exchange rate
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<ExchangeRateResponse>> deleteExchangeRate(@PathVariable UUID id) {
        log.info("Deleting exchange rate: {}", id);
        ExchangeRateResponse exchangeRate = exchangeRateService.deleteExchangeRate(id);
        return ResponseEntity.ok(ApiResponse.success("Exchange rate deleted successfully", exchangeRate));
    }

    /**
     * Get current active exchange rate
     */
    @GetMapping("/current")
    public ResponseEntity<ApiResponse<ExchangeRateResponse>> getCurrentActiveRate() {
        log.info("Getting current active exchange rate");
        ExchangeRateResponse exchangeRate = exchangeRateService.getCurrentActiveRate();
        return ResponseEntity.ok(ApiResponse.success("Current exchange rate retrieved successfully", exchangeRate));
    }

    /**
     * Get current rate value only (for calculations)
     */
    @GetMapping("/current/value")
    public ResponseEntity<ApiResponse<Double>> getCurrentRateValue() {
        log.info("Getting current exchange rate value");
        Double rate = exchangeRateService.getCurrentRateValue();
        return ResponseEntity.ok(ApiResponse.success("Current exchange rate value retrieved successfully", rate));
    }
}
package com.emenu.features.auth.controller;

import com.emenu.features.auth.dto.request.BusinessSettingsRequest;
import com.emenu.features.auth.dto.response.BusinessSettingsResponse;
import com.emenu.features.auth.service.BusinessSettingsService;
import com.emenu.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/business/settings")
@RequiredArgsConstructor
@Slf4j
public class BusinessSettingsController {

    private final BusinessSettingsService businessSettingsService;

    /**
     * Get business settings
     */
    @GetMapping("/{businessId}")
    public ResponseEntity<ApiResponse<BusinessSettingsResponse>> getBusinessSettings(@PathVariable UUID businessId) {
        log.info("Getting business settings for: {}", businessId);
        BusinessSettingsResponse settings = businessSettingsService.getBusinessSettings(businessId);
        return ResponseEntity.ok(ApiResponse.success("Business settings retrieved successfully", settings));
    }

    /**
     * Update business settings
     */
    @PutMapping("/{businessId}")
    public ResponseEntity<ApiResponse<BusinessSettingsResponse>> updateBusinessSettings(
            @PathVariable UUID businessId,
            @Valid @RequestBody BusinessSettingsRequest request) {
        log.info("Updating business settings for: {} - Exchange rate: {}", businessId, request.getUsdToKhrRate());
        BusinessSettingsResponse settings = businessSettingsService.updateBusinessSettings(businessId, request);
        return ResponseEntity.ok(ApiResponse.success("Business settings updated successfully", settings));
    }

    /**
     * Get my business settings - For current logged in business user
     */
    @GetMapping("/my-business")
    public ResponseEntity<ApiResponse<BusinessSettingsResponse>> getMyBusinessSettings() {
        log.info("Getting settings for current user's business");
        BusinessSettingsResponse settings = businessSettingsService.getCurrentUserBusinessSettings();
        return ResponseEntity.ok(ApiResponse.success("Business settings retrieved successfully", settings));
    }

    /**
     * Update my business settings - For current logged in business owner/manager
     */
    @PutMapping("/my-business")
    public ResponseEntity<ApiResponse<BusinessSettingsResponse>> updateMyBusinessSettings(
            @Valid @RequestBody BusinessSettingsRequest request) {
        log.info("Updating settings for current user's business - Exchange rate: {}", request.getUsdToKhrRate());
        BusinessSettingsResponse settings = businessSettingsService.updateCurrentUserBusinessSettings(request);
        return ResponseEntity.ok(ApiResponse.success("Business settings updated successfully", settings));
    }
}
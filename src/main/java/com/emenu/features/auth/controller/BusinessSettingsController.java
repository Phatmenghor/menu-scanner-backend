package com.emenu.features.auth.controller;

import com.emenu.features.auth.dto.request.BusinessSettingsRequest;
import com.emenu.features.auth.dto.response.BusinessSettingsResponse;
import com.emenu.features.auth.service.BusinessSettingsService;
import com.emenu.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/business/settings")
@RequiredArgsConstructor
@Slf4j
public class BusinessSettingsController {

    private final BusinessSettingsService businessSettingsService;

    /**
     * Get business settings - Platform admin or business users can access
     */
    @GetMapping("/{businessId}")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN') or " +
                  "(hasAnyRole('BUSINESS_OWNER', 'BUSINESS_MANAGER', 'BUSINESS_STAFF') and @securityUtils.hasBusinessAccess(#businessId))")
    public ResponseEntity<ApiResponse<BusinessSettingsResponse>> getBusinessSettings(@PathVariable UUID businessId) {
        log.info("Getting business settings for: {}", businessId);
        BusinessSettingsResponse settings = businessSettingsService.getBusinessSettings(businessId);
        return ResponseEntity.ok(ApiResponse.success("Business settings retrieved successfully", settings));
    }

    /**
     * Update business settings - Only business owner/manager or platform admin
     */
    @PutMapping("/{businessId}")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN') or " +
                  "(hasAnyRole('BUSINESS_OWNER', 'BUSINESS_MANAGER') and @securityUtils.hasBusinessAccess(#businessId))")
    public ResponseEntity<ApiResponse<BusinessSettingsResponse>> updateBusinessSettings(
            @PathVariable UUID businessId,
            @Valid @RequestBody BusinessSettingsRequest request) {
        log.info("Updating business settings for: {} - Exchange rate: {}", businessId, request.getUsdToKhrRate());
        BusinessSettingsResponse settings = businessSettingsService.updateBusinessSettings(businessId, request);
        return ResponseEntity.ok(ApiResponse.success("Business settings updated successfully", settings));
    }

    /**
     * Upload business logo
     */
    @PostMapping("/{businessId}/logo")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN') or " +
                  "(hasAnyRole('BUSINESS_OWNER', 'BUSINESS_MANAGER') and @securityUtils.hasBusinessAccess(#businessId))")
    public ResponseEntity<ApiResponse<String>> uploadLogo(
            @PathVariable UUID businessId,
            @RequestParam String logoUrl) {
        log.info("Uploading logo for business: {}", businessId);
        String updatedLogoUrl = businessSettingsService.updateLogo(businessId, logoUrl);
        return ResponseEntity.ok(ApiResponse.success("Logo updated successfully", updatedLogoUrl));
    }

    /**
     * Get my business settings - For current logged in business user
     */
    @GetMapping("/my-business")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'BUSINESS_MANAGER', 'BUSINESS_STAFF')")
    public ResponseEntity<ApiResponse<BusinessSettingsResponse>> getMyBusinessSettings() {
        log.info("Getting settings for current user's business");
        BusinessSettingsResponse settings = businessSettingsService.getCurrentUserBusinessSettings();
        return ResponseEntity.ok(ApiResponse.success("Business settings retrieved successfully", settings));
    }

    /**
     * Update my business settings - For current logged in business owner/manager
     */
    @PutMapping("/my-business")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'BUSINESS_MANAGER')")
    public ResponseEntity<ApiResponse<BusinessSettingsResponse>> updateMyBusinessSettings(
            @Valid @RequestBody BusinessSettingsRequest request) {
        log.info("Updating settings for current user's business - Exchange rate: {}", request.getUsdToKhrRate());
        BusinessSettingsResponse settings = businessSettingsService.updateCurrentUserBusinessSettings(request);
        return ResponseEntity.ok(ApiResponse.success("Business settings updated successfully", settings));
    }

    /**
     * Update only exchange rate - Quick update for currency
     */
    @PatchMapping("/{businessId}/exchange-rate")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN') or " +
                  "(hasAnyRole('BUSINESS_OWNER', 'BUSINESS_MANAGER') and @securityUtils.hasBusinessAccess(#businessId))")
    public ResponseEntity<ApiResponse<BusinessSettingsResponse>> updateExchangeRate(
            @PathVariable UUID businessId,
            @RequestParam Double usdToKhrRate) {
        log.info("Updating exchange rate for business: {} to {}", businessId, usdToKhrRate);
        
        // Validation
        if (usdToKhrRate < 1000.0 || usdToKhrRate > 10000.0) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Exchange rate must be between 1000 and 10000 KHR per USD"));
        }
        
        BusinessSettingsRequest request = new BusinessSettingsRequest();
        request.setUsdToKhrRate(usdToKhrRate);
        
        BusinessSettingsResponse settings = businessSettingsService.updateBusinessSettings(businessId, request);
        return ResponseEntity.ok(ApiResponse.success("Exchange rate updated successfully", settings));
    }

    /**
     * Update my business exchange rate - For current user
     */
    @PatchMapping("/my-business/exchange-rate")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'BUSINESS_MANAGER')")
    public ResponseEntity<ApiResponse<BusinessSettingsResponse>> updateMyExchangeRate(
            @RequestParam Double usdToKhrRate) {
        log.info("Updating exchange rate for current user's business to {}", usdToKhrRate);
        
        // Validation
        if (usdToKhrRate < 1000.0 || usdToKhrRate > 10000.0) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Exchange rate must be between 1000 and 10000 KHR per USD"));
        }
        
        BusinessSettingsRequest request = new BusinessSettingsRequest();
        request.setUsdToKhrRate(usdToKhrRate);
        
        BusinessSettingsResponse settings = businessSettingsService.updateCurrentUserBusinessSettings(request);
        return ResponseEntity.ok(ApiResponse.success("Exchange rate updated successfully", settings));
    }
}
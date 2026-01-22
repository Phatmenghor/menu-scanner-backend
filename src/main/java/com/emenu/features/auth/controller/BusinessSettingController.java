package com.emenu.features.auth.controller;

import com.emenu.features.auth.dto.request.BusinessSettingCreateRequest;
import com.emenu.features.auth.dto.response.BusinessSettingResponse;
import com.emenu.features.auth.dto.update.BusinessSettingUpdateRequest;
import com.emenu.features.auth.service.BusinessSettingService;
import com.emenu.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/business-settings")
@RequiredArgsConstructor
@Slf4j
public class BusinessSettingController {

    private final BusinessSettingService businessSettingService;

    /**
     * Retrieves the current authenticated user's business settings
     */
    @GetMapping("/current")
    public ResponseEntity<ApiResponse<BusinessSettingResponse>> getCurrentBusinessSetting() {
        log.info("Get current business setting");
        BusinessSettingResponse response = businessSettingService.getCurrentBusinessSetting();
        return ResponseEntity.ok(ApiResponse.success("Business setting retrieved", response));
    }

    /**
     * Retrieves business settings for a specific business
     */
    @GetMapping("/business/{businessId}")
    public ResponseEntity<ApiResponse<BusinessSettingResponse>> getBusinessSettingByBusinessId(
            @PathVariable UUID businessId) {
        log.info("Get business setting for: {}", businessId);
        BusinessSettingResponse response = businessSettingService.getBusinessSettingByBusinessId(businessId);
        return ResponseEntity.ok(ApiResponse.success("Business setting retrieved", response));
    }

    /**
     * Creates new business settings
     */
    @PostMapping
    public ResponseEntity<ApiResponse<BusinessSettingResponse>> createBusinessSetting(
            @Valid @RequestBody BusinessSettingCreateRequest request) {
        log.info("Create business setting");
        BusinessSettingResponse response = businessSettingService.createBusinessSetting(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Business setting created", response));
    }

    /**
     * Updates business settings for a specific business
     */
    @PutMapping("/business/{businessId}")
    public ResponseEntity<ApiResponse<BusinessSettingResponse>> updateBusinessSetting(
            @PathVariable UUID businessId,
            @Valid @RequestBody BusinessSettingUpdateRequest request) {
        log.info("Update business setting: {}", businessId);
        BusinessSettingResponse response = businessSettingService.updateBusinessSetting(businessId, request);
        return ResponseEntity.ok(ApiResponse.success("Business setting updated", response));
    }

    /**
     * Deletes business settings for a specific business
     */
    @DeleteMapping("/business/{businessId}")
    public ResponseEntity<ApiResponse<Void>> deleteBusinessSetting(@PathVariable UUID businessId) {
        log.info("Delete business setting: {}", businessId);
        businessSettingService.deleteBusinessSetting(businessId);
        return ResponseEntity.ok(ApiResponse.success("Business setting deleted", null));
    }
}
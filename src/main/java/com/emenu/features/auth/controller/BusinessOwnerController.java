package com.emenu.features.auth.controller;

import com.emenu.features.auth.dto.filter.BusinessOwnerFilterRequest;
import com.emenu.features.auth.dto.request.*;
import com.emenu.features.auth.dto.response.BusinessOwnerCreateResponse;
import com.emenu.features.auth.dto.response.BusinessOwnerDetailResponse;
import com.emenu.features.auth.service.BusinessOwnerService;
import com.emenu.shared.dto.ApiResponse;
import com.emenu.shared.dto.PaginationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/business-owners")
@RequiredArgsConstructor
@Slf4j
public class BusinessOwnerController {

    private final BusinessOwnerService businessOwnerService;

    /**
     * Creates a new business owner with associated business, settings, and subscription
     */
    @PostMapping
    public ResponseEntity<ApiResponse<BusinessOwnerCreateResponse>> createBusinessOwner(
            @Valid @RequestBody BusinessOwnerCreateRequest request) {
        
        log.info("Creating business owner: {}", request.getBusinessName());
        BusinessOwnerCreateResponse response = businessOwnerService.createBusinessOwner(request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Business owner created successfully with " + response.getCreatedComponents().size() + " components", 
                        response
                ));
    }

    /**
     * Retrieves all business owners with pagination and filtering
     */
    @PostMapping("/all")
    public ResponseEntity<ApiResponse<PaginationResponse<BusinessOwnerDetailResponse>>> getAllBusinessOwners(
            @Valid @RequestBody BusinessOwnerFilterRequest request) {
        
        log.info("Getting business owners - Page: {}, Search: {}", request.getPageNo(), request.getSearch());
        PaginationResponse<BusinessOwnerDetailResponse> response = businessOwnerService.getAllBusinessOwners(request);
        
        return ResponseEntity.ok(ApiResponse.success(
                String.format("Found %d business owners (Page %d of %d)", 
                        response.getTotalElements(), response.getPageNo(), response.getTotalPages()),
                response
        ));
    }

    /**
     * Retrieves detailed information for a specific business owner
     */
    @GetMapping("/{ownerId}")
    public ResponseEntity<ApiResponse<BusinessOwnerDetailResponse>> getBusinessOwnerDetail(
            @PathVariable UUID ownerId) {
        
        log.info("Getting business owner detail: {}", ownerId);
        BusinessOwnerDetailResponse response = businessOwnerService.getBusinessOwnerDetail(ownerId);
        
        return ResponseEntity.ok(ApiResponse.success(
                "Business owner details retrieved successfully", 
                response
        ));
    }

    /**
     * Renews a business owner's subscription with optional plan change
     */
    @PostMapping("/{ownerId}/renew")
    public ResponseEntity<ApiResponse<BusinessOwnerDetailResponse>> renewSubscription(
            @PathVariable UUID ownerId,
            @Valid @RequestBody BusinessOwnerSubscriptionRenewRequest request) {
        
        log.info("Renewing subscription for business owner: {}", ownerId);
        BusinessOwnerDetailResponse response = businessOwnerService.renewSubscription(ownerId, request);
        
        String message = request.getNewPlanId() != null 
                ? "Subscription renewed with plan change" 
                : "Subscription renewed successfully";
        
        return ResponseEntity.ok(ApiResponse.success(message, response));
    }

    /**
     * Changes the subscription plan for a business owner
     */
    @PutMapping("/{ownerId}/change-plan")
    public ResponseEntity<ApiResponse<BusinessOwnerDetailResponse>> changePlan(
            @PathVariable UUID ownerId,
            @Valid @RequestBody BusinessOwnerChangePlanRequest request) {
        
        log.info("Changing plan for business owner: {}", ownerId);
        BusinessOwnerDetailResponse response = businessOwnerService.changePlan(ownerId, request);
        
        return ResponseEntity.ok(ApiResponse.success("Subscription plan changed successfully", response));
    }

    /**
     * Cancels a business owner's subscription with optional refund processing
     */
    @PostMapping("/{ownerId}/cancel")
    public ResponseEntity<ApiResponse<BusinessOwnerDetailResponse>> cancelSubscription(
            @PathVariable UUID ownerId,
            @Valid @RequestBody BusinessOwnerSubscriptionCancelRequest request) {
        
        log.info("Cancelling subscription for business owner: {}", ownerId);
        BusinessOwnerDetailResponse response = businessOwnerService.cancelSubscription(ownerId, request);
        
        String message = request.hasRefundAmount() 
                ? "Subscription cancelled with refund processed" 
                : "Subscription cancelled successfully";
        
        return ResponseEntity.ok(ApiResponse.success(message, response));
    }

    /**
     * Deletes a business owner and all associated data
     */
    @DeleteMapping("/{ownerId}")
    public ResponseEntity<ApiResponse<BusinessOwnerDetailResponse>> deleteBusinessOwner(
            @PathVariable UUID ownerId) {
        
        log.info("Deleting business owner: {}", ownerId);
        BusinessOwnerDetailResponse response = businessOwnerService.deleteBusinessOwner(ownerId);
        
        return ResponseEntity.ok(ApiResponse.success(
                "Business owner and related data deleted successfully", 
                response
        ));
    }
}
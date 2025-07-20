package com.emenu.features.subscription.controller;

import com.emenu.features.subscription.dto.request.SubscriptionCreateRequest;
import com.emenu.features.subscription.dto.resposne.SubscriptionResponse;
import com.emenu.features.subscription.service.SubscriptionService;
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
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
@Slf4j
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping("/business/{businessId}")
    @PreAuthorize("hasRole('PLATFORM_OWNER') or hasRole('BUSINESS_OWNER')")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> createSubscription(
            @PathVariable UUID businessId,
            @Valid @RequestBody SubscriptionCreateRequest request) {
        log.info("Creating subscription for business: {}", businessId);
        SubscriptionResponse subscription = subscriptionService.createSubscription(businessId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Subscription created successfully", subscription));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('PLATFORM_OWNER') or hasRole('BUSINESS_OWNER')")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> getSubscriptionById(@PathVariable UUID id) {
        log.info("Getting subscription by ID: {}", id);
        SubscriptionResponse subscription = subscriptionService.getSubscriptionById(id);
        return ResponseEntity.ok(ApiResponse.success("Subscription retrieved successfully", subscription));
    }

    @GetMapping("/business/{businessId}")
    @PreAuthorize("hasRole('PLATFORM_OWNER') or hasRole('BUSINESS_OWNER')")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> getSubscriptionByBusinessId(@PathVariable UUID businessId) {
        log.info("Getting subscription for business: {}", businessId);
        SubscriptionResponse subscription = subscriptionService.getSubscriptionByBusinessId(businessId);
        return ResponseEntity.ok(ApiResponse.success("Subscription retrieved successfully", subscription));
    }

    @GetMapping
    @PreAuthorize("hasRole('PLATFORM_OWNER')")
    public ResponseEntity<ApiResponse<PaginationResponse<SubscriptionResponse>>> getAllSubscriptions(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        log.info("Getting all subscriptions");
        PaginationResponse<SubscriptionResponse> subscriptions = subscriptionService.getAllSubscriptions(pageNo, pageSize);
        return ResponseEntity.ok(ApiResponse.success("Subscriptions retrieved successfully", subscriptions));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PLATFORM_OWNER') or hasRole('BUSINESS_OWNER')")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> updateSubscription(
            @PathVariable UUID id,
            @Valid @RequestBody SubscriptionCreateRequest request) {
        log.info("Updating subscription: {}", id);
        SubscriptionResponse subscription = subscriptionService.updateSubscription(id, request);
        return ResponseEntity.ok(ApiResponse.success("Subscription updated successfully", subscription));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('PLATFORM_OWNER') or hasRole('BUSINESS_OWNER')")
    public ResponseEntity<ApiResponse<Void>> cancelSubscription(@PathVariable UUID id) {
        log.info("Cancelling subscription: {}", id);
        subscriptionService.cancelSubscription(id);
        return ResponseEntity.ok(ApiResponse.success("Subscription cancelled successfully", null));
    }

    @PostMapping("/{id}/renew")
    @PreAuthorize("hasRole('PLATFORM_OWNER') or hasRole('BUSINESS_OWNER')")
    public ResponseEntity<ApiResponse<Void>> renewSubscription(@PathVariable UUID id) {
        log.info("Renewing subscription: {}", id);
        subscriptionService.renewSubscription(id);
        return ResponseEntity.ok(ApiResponse.success("Subscription renewed successfully", null));
    }

    // Usage validation endpoints
    @GetMapping("/business/{businessId}/can-add-staff")
    @PreAuthorize("hasRole('BUSINESS_OWNER') or hasRole('BUSINESS_MANAGER')")
    public ResponseEntity<ApiResponse<Boolean>> canAddStaff(@PathVariable UUID businessId) {
        boolean canAdd = subscriptionService.canAddStaff(businessId);
        return ResponseEntity.ok(ApiResponse.success("Staff addition check completed", canAdd));
    }

    @GetMapping("/business/{businessId}/can-add-menu-item")
    @PreAuthorize("hasRole('BUSINESS_OWNER') or hasRole('BUSINESS_MANAGER')")
    public ResponseEntity<ApiResponse<Boolean>> canAddMenuItem(@PathVariable UUID businessId) {
        boolean canAdd = subscriptionService.canAddMenuItem(businessId);
        return ResponseEntity.ok(ApiResponse.success("Menu item addition check completed", canAdd));
    }

    @GetMapping("/business/{businessId}/can-add-table")
    @PreAuthorize("hasRole('BUSINESS_OWNER') or hasRole('BUSINESS_MANAGER')")
    public ResponseEntity<ApiResponse<Boolean>> canAddTable(@PathVariable UUID businessId) {
        boolean canAdd = subscriptionService.canAddTable(businessId);
        return ResponseEntity.ok(ApiResponse.success("Table addition check completed", canAdd));
    }
}
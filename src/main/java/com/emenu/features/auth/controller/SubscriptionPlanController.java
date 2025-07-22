package com.emenu.features.auth.controller;

import com.emenu.features.auth.dto.filter.SubscriptionPlanFilterRequest;
import com.emenu.features.auth.dto.request.SubscriptionPlanCreateRequest;
import com.emenu.features.auth.dto.response.SubscriptionPlanResponse;
import com.emenu.features.auth.dto.update.SubscriptionPlanUpdateRequest;
import com.emenu.features.auth.service.SubscriptionPlanService;
import com.emenu.shared.dto.ApiResponse;
import com.emenu.shared.dto.PaginationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/subscription-plans")
@RequiredArgsConstructor
@Slf4j
public class SubscriptionPlanController {

    private final SubscriptionPlanService subscriptionPlanService;

    /**
     * Get all subscription plans with filtering and pagination
     */
    @PostMapping("/all")
    public ResponseEntity<ApiResponse<PaginationResponse<SubscriptionPlanResponse>>> getAllPlans(
            @Valid @RequestBody SubscriptionPlanFilterRequest filter) {
        log.info("Getting subscription plans with filter - Status: {}, Search: {}", filter.getStatus(), filter.getSearch());
        PaginationResponse<SubscriptionPlanResponse> plans = subscriptionPlanService.getAllPlans(filter);
        return ResponseEntity.ok(ApiResponse.success("Subscription plans retrieved successfully", plans));
    }

    /**
     * Get public subscription plans (for frontend display)
     */
    @GetMapping("/public")
    public ResponseEntity<ApiResponse<List<SubscriptionPlanResponse>>> getPublicPlans() {
        log.info("Getting public subscription plans");
        List<SubscriptionPlanResponse> plans = subscriptionPlanService.getPublicPlans();
        return ResponseEntity.ok(ApiResponse.success("Public subscription plans retrieved successfully", plans));
    }

    /**
     * Get subscription plan by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SubscriptionPlanResponse>> getPlanById(@PathVariable UUID id) {
        log.info("Getting subscription plan by ID: {}", id);
        SubscriptionPlanResponse plan = subscriptionPlanService.getPlanById(id);
        return ResponseEntity.ok(ApiResponse.success("Subscription plan retrieved successfully", plan));
    }

    /**
     * Create new subscription plan
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<SubscriptionPlanResponse>> createPlan(@Valid @RequestBody SubscriptionPlanCreateRequest request) {
        log.info("Creating subscription plan: {}", request.getName());
        SubscriptionPlanResponse plan = subscriptionPlanService.createPlan(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Subscription plan created successfully", plan));
    }

    /**
     * Update subscription plan (unified update endpoint)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<SubscriptionPlanResponse>> updatePlan(
            @PathVariable UUID id,
            @Valid @RequestBody SubscriptionPlanUpdateRequest request) {
        log.info("Updating subscription plan: {}", id);
        SubscriptionPlanResponse plan = subscriptionPlanService.updatePlan(id, request);
        return ResponseEntity.ok(ApiResponse.success("Subscription plan updated successfully", plan));
    }

    /**
     * Delete subscription plan
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deletePlan(@PathVariable UUID id) {
        log.info("Deleting subscription plan: {}", id);
        subscriptionPlanService.deletePlan(id);
        return ResponseEntity.ok(ApiResponse.success("Subscription plan deleted successfully", null));
    }

    /**
     * Create custom plan for specific business
     */
    @PostMapping("/custom/{businessId}")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<SubscriptionPlanResponse>> createCustomPlan(
            @PathVariable UUID businessId,
            @Valid @RequestBody SubscriptionPlanCreateRequest request) {
        log.info("Creating custom subscription plan for business: {}", businessId);
        SubscriptionPlanResponse plan = subscriptionPlanService.createCustomPlan(businessId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Custom subscription plan created successfully", plan));
    }

    /**
     * Get custom plans for business
     */
    @GetMapping("/custom/{businessId}")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN') or @securityUtils.hasBusinessAccess(#businessId)")
    public ResponseEntity<ApiResponse<List<SubscriptionPlanResponse>>> getCustomPlansForBusiness(@PathVariable UUID businessId) {
        log.info("Getting custom subscription plans for business: {}", businessId);
        List<SubscriptionPlanResponse> plans = subscriptionPlanService.getCustomPlansForBusiness(businessId);
        return ResponseEntity.ok(ApiResponse.success("Custom subscription plans retrieved successfully", plans));
    }

    /**
     * Assign plan to business (create subscription)
     */
    @PostMapping("/{planId}/assign/{businessId}")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN', 'BUSINESS_OWNER')")
    public ResponseEntity<ApiResponse<SubscriptionPlanResponse>> assignPlanToBusiness(
            @PathVariable UUID planId,
            @PathVariable UUID businessId,
            @RequestParam(defaultValue = "false") Boolean autoRenew,
            @RequestParam(required = false) Integer customDurationDays) {
        log.info("Assigning plan {} to business: {}", planId, businessId);
        SubscriptionPlanResponse result = subscriptionPlanService.assignPlanToBusiness(planId, businessId, autoRenew, customDurationDays);
        return ResponseEntity.ok(ApiResponse.success("Plan assigned to business successfully", result));
    }

    /**
     * Bulk assign plan to multiple businesses
     */
    @PostMapping("/{planId}/assign/bulk")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<List<SubscriptionPlanResponse>>> bulkAssignPlan(
            @PathVariable UUID planId,
            @RequestBody List<UUID> businessIds,
            @RequestParam(defaultValue = "false") Boolean autoRenew) {
        log.info("Bulk assigning plan {} to {} businesses", planId, businessIds.size());
        List<SubscriptionPlanResponse> results = subscriptionPlanService.bulkAssignPlan(planId, businessIds, autoRenew);
        return ResponseEntity.ok(ApiResponse.success("Plan assigned to businesses successfully", results));
    }

    /**
     * Get plan statistics
     */
    @GetMapping("/{planId}/statistics")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<Object>> getPlanStatistics(@PathVariable UUID planId) {
        log.info("Getting statistics for plan: {}", planId);
        Object statistics = subscriptionPlanService.getPlanStatistics(planId);
        return ResponseEntity.ok(ApiResponse.success("Plan statistics retrieved successfully", statistics));
    }

    /**
     * Compare multiple plans
     */
    @PostMapping("/compare")
    public ResponseEntity<ApiResponse<Object>> comparePlans(@RequestBody List<UUID> planIds) {
        log.info("Comparing {} plans", planIds.size());
        Object comparison = subscriptionPlanService.comparePlans(planIds);
        return ResponseEntity.ok(ApiResponse.success("Plans compared successfully", comparison));
    }

    /**
     * Get recommended plans for business
     */
    @GetMapping("/recommended/{businessId}")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN') or @securityUtils.hasBusinessAccess(#businessId)")
    public ResponseEntity<ApiResponse<List<SubscriptionPlanResponse>>> getRecommendedPlans(@PathVariable UUID businessId) {
        log.info("Getting recommended plans for business: {}", businessId);
        List<SubscriptionPlanResponse> plans = subscriptionPlanService.getRecommendedPlans(businessId);
        return ResponseEntity.ok(ApiResponse.success("Recommended plans retrieved successfully", plans));
    }

    /**
     * Seed default plans - Development/Setup endpoint
     */
    @PostMapping("/seed")
    @PreAuthorize("hasRole('PLATFORM_OWNER')")
    public ResponseEntity<ApiResponse<Void>> seedDefaultPlans() {
        log.info("Seeding default subscription plans");
        subscriptionPlanService.seedDefaultPlans();
        return ResponseEntity.ok(ApiResponse.success("Default subscription plans seeded successfully", null));
    }
}
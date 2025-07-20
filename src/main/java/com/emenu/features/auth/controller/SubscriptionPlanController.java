package com.emenu.features.auth.controller;

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
     * Get public subscription plans (for frontend display)
     * Available to all authenticated users
     */
    @GetMapping("/public")
    public ResponseEntity<ApiResponse<List<SubscriptionPlanResponse>>> getPublicPlans() {
        log.info("Getting public subscription plans");
        List<SubscriptionPlanResponse> plans = subscriptionPlanService.getPublicPlans();
        return ResponseEntity.ok(ApiResponse.success("Public subscription plans retrieved successfully", plans));
    }

    /**
     * Get all active subscription plans
     * Available to platform users and business owners
     */
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN', 'BUSINESS_OWNER', 'BUSINESS_MANAGER')")
    public ResponseEntity<ApiResponse<List<SubscriptionPlanResponse>>> getActivePlans() {
        log.info("Getting active subscription plans");
        List<SubscriptionPlanResponse> plans = subscriptionPlanService.getAllActivePlans();
        return ResponseEntity.ok(ApiResponse.success("Active subscription plans retrieved successfully", plans));
    }

    /**
     * Get all subscription plans with pagination
     * Only platform admins can see all plans including inactive ones
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<PaginationResponse<SubscriptionPlanResponse>>> getAllPlans(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        log.info("Getting all subscription plans - Page: {}, Size: {}", pageNo, pageSize);
        PaginationResponse<SubscriptionPlanResponse> plans = subscriptionPlanService.getAllPlans(pageNo, pageSize);
        return ResponseEntity.ok(ApiResponse.success("Subscription plans retrieved successfully", plans));
    }

    /**
     * Get subscription plan by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN', 'BUSINESS_OWNER', 'BUSINESS_MANAGER')")
    public ResponseEntity<ApiResponse<SubscriptionPlanResponse>> getPlanById(@PathVariable UUID id) {
        log.info("Getting subscription plan by ID: {}", id);
        SubscriptionPlanResponse plan = subscriptionPlanService.getPlanById(id);
        return ResponseEntity.ok(ApiResponse.success("Subscription plan retrieved successfully", plan));
    }

    /**
     * Create new subscription plan
     * Only platform admins can create plans
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
     * Update subscription plan
     * Only platform admins can update plans
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
     * Only platform admins can delete plans
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deletePlan(@PathVariable UUID id) {
        log.info("Deleting subscription plan: {}", id);
        subscriptionPlanService.deletePlan(id);
        return ResponseEntity.ok(ApiResponse.success("Subscription plan deleted successfully", null));
    }

    /**
     * Activate subscription plan
     */
    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> activatePlan(@PathVariable UUID id) {
        log.info("Activating subscription plan: {}", id);
        subscriptionPlanService.activatePlan(id);
        return ResponseEntity.ok(ApiResponse.success("Subscription plan activated successfully", null));
    }

    /**
     * Deactivate subscription plan
     */
    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deactivatePlan(@PathVariable UUID id) {
        log.info("Deactivating subscription plan: {}", id);
        subscriptionPlanService.deactivatePlan(id);
        return ResponseEntity.ok(ApiResponse.success("Subscription plan deactivated successfully", null));
    }

    /**
     * Set plan as default
     */
    @PostMapping("/{id}/set-default")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<SubscriptionPlanResponse>> setAsDefault(@PathVariable UUID id) {
        log.info("Setting subscription plan as default: {}", id);
        SubscriptionPlanResponse plan = subscriptionPlanService.setAsDefault(id);
        return ResponseEntity.ok(ApiResponse.success("Default plan set successfully", plan));
    }

    /**
     * Create custom plan for specific business
     * Platform admins can create custom plans for businesses
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
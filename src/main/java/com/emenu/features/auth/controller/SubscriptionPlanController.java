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
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<ApiResponse<Void>> deletePlan(@PathVariable UUID id) {
        log.info("Deleting subscription plan: {}", id);
        subscriptionPlanService.deletePlan(id);
        return ResponseEntity.ok(ApiResponse.success("Subscription plan deleted successfully", null));
    }
}
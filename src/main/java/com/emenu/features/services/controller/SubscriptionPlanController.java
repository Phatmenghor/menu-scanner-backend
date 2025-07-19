package com.emenu.features.services.controller;

import com.emenu.features.services.dto.request.CreatePlanRequest;
import com.emenu.features.services.dto.request.UpdatePlanRequest;
import com.emenu.features.services.dto.response.PlanResponse;
import com.emenu.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/subscription-plans")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Subscription Plans", description = "Subscription plan management")
public class SubscriptionPlanController {

    private final SubscriptionPlanService subscriptionPlanService;

    @PostMapping
    @Operation(summary = "Create subscription plan")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<PlanResponse>> createPlan(
            @Valid @RequestBody CreatePlanRequest request) {
        log.info("Creating subscription plan: {}", request.getName());
        PlanResponse response = subscriptionPlanService.createPlan(request);
        return ResponseEntity.ok(ApiResponse.success("Subscription plan created successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get subscription plan by ID")
    public ResponseEntity<ApiResponse<PlanResponse>> getPlan(@PathVariable UUID id) {
        log.info("Getting subscription plan: {}", id);
        PlanResponse response = subscriptionPlanService.getPlan(id);
        return ResponseEntity.ok(ApiResponse.success("Subscription plan retrieved successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update subscription plan")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<PlanResponse>> updatePlan(
            @PathVariable UUID id, @Valid @RequestBody UpdatePlanRequest request) {
        log.info("Updating subscription plan: {}", id);
        PlanResponse response = subscriptionPlanService.updatePlan(id, request);
        return ResponseEntity.ok(ApiResponse.success("Subscription plan updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete subscription plan")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deletePlan(@PathVariable UUID id) {
        log.info("Deleting subscription plan: {}", id);
        subscriptionPlanService.deletePlan(id);
        return ResponseEntity.ok(ApiResponse.success("Subscription plan deleted successfully", null));
    }

    @GetMapping
    @Operation(summary = "List all subscription plans")
    public ResponseEntity<ApiResponse<List<PlanResponse>>> listPlans(
            @RequestParam(defaultValue = "true") Boolean activeOnly) {
        log.info("Listing subscription plans");
        List<PlanResponse> response = subscriptionPlanService.listPlans(activeOnly);
        return ResponseEntity.ok(ApiResponse.success("Subscription plans retrieved successfully", response));
    }

    @PostMapping("/{id}/activate")
    @Operation(summary = "Activate subscription plan")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> activatePlan(@PathVariable UUID id) {
        log.info("Activating subscription plan: {}", id);
        subscriptionPlanService.activatePlan(id);
        return ResponseEntity.ok(ApiResponse.success("Subscription plan activated successfully", null));
    }

    @PostMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate subscription plan")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deactivatePlan(@PathVariable UUID id) {
        log.info("Deactivating subscription plan: {}", id);
        subscriptionPlanService.deactivatePlan(id);
        return ResponseEntity.ok(ApiResponse.success("Subscription plan deactivated successfully", null));
    }
}
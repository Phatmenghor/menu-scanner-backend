package com.emenu.features.auth.controller;

import com.emenu.features.auth.dto.filter.SubscriptionFilterRequest;
import com.emenu.features.auth.dto.request.SubscriptionCreateRequest;
import com.emenu.features.auth.dto.response.SubscriptionResponse;
import com.emenu.features.auth.dto.update.SubscriptionUpdateRequest;
import com.emenu.features.auth.service.SubscriptionService;
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
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
@Slf4j
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping
    public ResponseEntity<ApiResponse<SubscriptionResponse>> createSubscription(@Valid @RequestBody SubscriptionCreateRequest request) {
        log.info("Creating subscription for business: {}", request.getBusinessId());
        SubscriptionResponse subscription = subscriptionService.createSubscription(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Subscription created successfully", subscription));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PaginationResponse<SubscriptionResponse>>> getSubscriptions(@ModelAttribute SubscriptionFilterRequest filter) {
        log.info("Getting subscriptions with filter");
        // ✅ Service returns pagination response directly from mapper
        PaginationResponse<SubscriptionResponse> subscriptions = subscriptionService.getSubscriptions(filter);
        return ResponseEntity.ok(ApiResponse.success("Subscriptions retrieved successfully", subscriptions));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> getSubscriptionById(@PathVariable UUID id) {
        log.info("Getting subscription by ID: {}", id);
        SubscriptionResponse subscription = subscriptionService.getSubscriptionById(id);
        return ResponseEntity.ok(ApiResponse.success("Subscription retrieved successfully", subscription));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> updateSubscription(
            @PathVariable UUID id,
            @Valid @RequestBody SubscriptionUpdateRequest request) {
        log.info("Updating subscription: {}", id);
        SubscriptionResponse subscription = subscriptionService.updateSubscription(id, request);
        return ResponseEntity.ok(ApiResponse.success("Subscription updated successfully", subscription));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSubscription(@PathVariable UUID id) {
        log.info("Deleting subscription: {}", id);
        subscriptionService.deleteSubscription(id);
        return ResponseEntity.ok(ApiResponse.success("Subscription deleted successfully", null));
    }

    @PostMapping("/{id}/renew")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> renewSubscription(@PathVariable UUID id) {
        log.info("Renewing subscription: {}", id);
        SubscriptionResponse subscription = subscriptionService.renewSubscription(id);
        return ResponseEntity.ok(ApiResponse.success("Subscription renewed successfully", subscription));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelSubscription(@PathVariable UUID id) {
        log.info("Cancelling subscription: {}", id);
        subscriptionService.cancelSubscription(id);
        return ResponseEntity.ok(ApiResponse.success("Subscription cancelled successfully", null));
    }

    @GetMapping("/business/{businessId}")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> getActiveSubscriptionByBusiness(@PathVariable UUID businessId) {
        log.info("Getting active subscription for business: {}", businessId);
        SubscriptionResponse subscription = subscriptionService.getActiveSubscriptionByBusiness(businessId);
        return ResponseEntity.ok(ApiResponse.success("Active subscription retrieved successfully", subscription));
    }

    @GetMapping("/business/{businessId}/history")
    public ResponseEntity<ApiResponse<List<SubscriptionResponse>>> getBusinessSubscriptionHistory(@PathVariable UUID businessId) {
        log.info("Getting subscription history for business: {}", businessId);
        List<SubscriptionResponse> history = subscriptionService.getBusinessSubscriptionHistory(businessId);
        return ResponseEntity.ok(ApiResponse.success("Subscription history retrieved successfully", history));
    }

    @GetMapping("/expiring")
    public ResponseEntity<ApiResponse<List<SubscriptionResponse>>> getExpiringSubscriptions(
            @RequestParam(defaultValue = "7") int days) {
        log.info("Getting subscriptions expiring in {} days", days);
        List<SubscriptionResponse> expiring = subscriptionService.getExpiringSubscriptions(days);
        return ResponseEntity.ok(ApiResponse.success("Expiring subscriptions retrieved successfully", expiring));
    }

    @GetMapping("/expired")
    public ResponseEntity<ApiResponse<List<SubscriptionResponse>>> getExpiredSubscriptions() {
        log.info("Getting expired subscriptions");
        List<SubscriptionResponse> expired = subscriptionService.getExpiredSubscriptions();
        return ResponseEntity.ok(ApiResponse.success("Expired subscriptions retrieved successfully", expired));
    }

    @PostMapping("/process-expired")
    public ResponseEntity<ApiResponse<Void>> processExpiredSubscriptions() {
        log.info("Processing expired subscriptions");
        subscriptionService.processExpiredSubscriptions();
        return ResponseEntity.ok(ApiResponse.success("Expired subscriptions processed successfully", null));
    }
}
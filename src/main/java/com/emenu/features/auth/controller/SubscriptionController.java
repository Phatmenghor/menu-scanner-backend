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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
@Slf4j
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    /**
     * Get all subscriptions with filtering and pagination
     */
    @PostMapping("/all")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<PaginationResponse<SubscriptionResponse>>> getAllSubscriptions(
            @Valid @RequestBody SubscriptionFilterRequest filter) {
        log.info("Getting subscriptions with filter - Status: {}, BusinessId: {}", filter.getStatus(), filter.getBusinessId());
        PaginationResponse<SubscriptionResponse> subscriptions = subscriptionService.getSubscriptions(filter);
        return ResponseEntity.ok(ApiResponse.success("Subscriptions retrieved successfully", subscriptions));
    }

    /**
     * Get my business subscriptions (current user's business)
     */
    @PostMapping("/my-business")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'BUSINESS_MANAGER')")
    public ResponseEntity<ApiResponse<PaginationResponse<SubscriptionResponse>>> getMyBusinessSubscriptions(
            @Valid @RequestBody SubscriptionFilterRequest filter) {
        log.info("Getting current user's business subscriptions");
        PaginationResponse<SubscriptionResponse> subscriptions = subscriptionService.getCurrentUserBusinessSubscriptions(filter);
        return ResponseEntity.ok(ApiResponse.success("Business subscriptions retrieved successfully", subscriptions));
    }

    /**
     * Create new subscription
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN', 'BUSINESS_OWNER')")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> createSubscription(@Valid @RequestBody SubscriptionCreateRequest request) {
        log.info("Creating subscription for business: {}", request.getBusinessId());
        SubscriptionResponse subscription = subscriptionService.createSubscription(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Subscription created successfully", subscription));
    }

    /**
     * Get subscription by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN') or @subscriptionService.canAccessSubscription(#id)")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> getSubscriptionById(@PathVariable UUID id) {
        log.info("Getting subscription by ID: {}", id);
        SubscriptionResponse subscription = subscriptionService.getSubscriptionById(id);
        return ResponseEntity.ok(ApiResponse.success("Subscription retrieved successfully", subscription));
    }

    /**
     * Update subscription (unified update endpoint)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> updateSubscription(
            @PathVariable UUID id,
            @Valid @RequestBody SubscriptionUpdateRequest request) {
        log.info("Updating subscription: {}", id);
        SubscriptionResponse subscription = subscriptionService.updateSubscription(id, request);
        return ResponseEntity.ok(ApiResponse.success("Subscription updated successfully", subscription));
    }

    /**
     * Delete/Cancel subscription
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteSubscription(@PathVariable UUID id) {
        log.info("Deleting subscription: {}", id);
        subscriptionService.deleteSubscription(id);
        return ResponseEntity.ok(ApiResponse.success("Subscription deleted successfully", null));
    }

    /**
     * Get active subscription for business
     */
    @GetMapping("/business/{businessId}/active")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN') or @securityUtils.hasBusinessAccess(#businessId)")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> getActiveSubscriptionByBusiness(@PathVariable UUID businessId) {
        log.info("Getting active subscription for business: {}", businessId);
        SubscriptionResponse subscription = subscriptionService.getActiveSubscriptionByBusiness(businessId);
        return ResponseEntity.ok(ApiResponse.success("Active subscription retrieved successfully", subscription));
    }

    /**
     * Get my active subscription (current user's business)
     */
    @GetMapping("/my-business/active")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'BUSINESS_MANAGER', 'BUSINESS_STAFF')")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> getMyActiveSubscription() {
        log.info("Getting active subscription for current user's business");
        SubscriptionResponse subscription = subscriptionService.getCurrentUserActiveSubscription();
        return ResponseEntity.ok(ApiResponse.success("Active subscription retrieved successfully", subscription));
    }

    /**
     * Get subscription history for business
     */
    @GetMapping("/business/{businessId}/history")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN') or @securityUtils.hasBusinessAccess(#businessId)")
    public ResponseEntity<ApiResponse<List<SubscriptionResponse>>> getBusinessSubscriptionHistory(@PathVariable UUID businessId) {
        log.info("Getting subscription history for business: {}", businessId);
        List<SubscriptionResponse> history = subscriptionService.getBusinessSubscriptionHistory(businessId);
        return ResponseEntity.ok(ApiResponse.success("Subscription history retrieved successfully", history));
    }

    /**
     * Renew subscription
     */
    @PostMapping("/{id}/renew")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN', 'BUSINESS_OWNER')")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> renewSubscription(
            @PathVariable UUID id,
            @RequestParam(required = false) UUID newPlanId,
            @RequestParam(required = false) Integer customDurationDays) {
        log.info("Renewing subscription: {} with planId: {}", id, newPlanId);
        SubscriptionResponse subscription = subscriptionService.renewSubscription(id, newPlanId, customDurationDays);
        return ResponseEntity.ok(ApiResponse.success("Subscription renewed successfully", subscription));
    }

    /**
     * Cancel subscription
     */
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN', 'BUSINESS_OWNER')")
    public ResponseEntity<ApiResponse<Void>> cancelSubscription(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "false") Boolean immediate) {
        log.info("Cancelling subscription: {} immediately: {}", id, immediate);
        subscriptionService.cancelSubscription(id, immediate);
        return ResponseEntity.ok(ApiResponse.success("Subscription cancelled successfully", null));
    }

    /**
     * Suspend subscription
     */
    @PostMapping("/{id}/suspend")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> suspendSubscription(
            @PathVariable UUID id,
            @RequestParam(required = false) String reason) {
        log.info("Suspending subscription: {} with reason: {}", id, reason);
        SubscriptionResponse subscription = subscriptionService.suspendSubscription(id, reason);
        return ResponseEntity.ok(ApiResponse.success("Subscription suspended successfully", subscription));
    }

    /**
     * Reactivate subscription
     */
    @PostMapping("/{id}/reactivate")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> reactivateSubscription(@PathVariable UUID id) {
        log.info("Reactivating subscription: {}", id);
        SubscriptionResponse subscription = subscriptionService.reactivateSubscription(id);
        return ResponseEntity.ok(ApiResponse.success("Subscription reactivated successfully", subscription));
    }

    /**
     * Extend subscription
     */
    @PostMapping("/{id}/extend")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> extendSubscription(
            @PathVariable UUID id,
            @RequestParam Integer days,
            @RequestParam(required = false) String reason) {
        log.info("Extending subscription: {} by {} days", id, days);
        SubscriptionResponse subscription = subscriptionService.extendSubscription(id, days, reason);
        return ResponseEntity.ok(ApiResponse.success("Subscription extended successfully", subscription));
    }

    /**
     * Upgrade/Downgrade subscription plan
     */
    @PostMapping("/{id}/change-plan")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN', 'BUSINESS_OWNER')")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> changeSubscriptionPlan(
            @PathVariable UUID id,
            @RequestParam UUID newPlanId,
            @RequestParam(defaultValue = "false") Boolean immediate) {
        log.info("Changing subscription {} plan to: {} immediately: {}", id, newPlanId, immediate);
        SubscriptionResponse subscription = subscriptionService.changeSubscriptionPlan(id, newPlanId, immediate);
        return ResponseEntity.ok(ApiResponse.success("Subscription plan changed successfully", subscription));
    }

    /**
     * Get expiring subscriptions
     */
    @GetMapping("/expiring")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<List<SubscriptionResponse>>> getExpiringSubscriptions(
            @RequestParam(defaultValue = "7") int days) {
        log.info("Getting subscriptions expiring in {} days", days);
        List<SubscriptionResponse> expiring = subscriptionService.getExpiringSubscriptions(days);
        return ResponseEntity.ok(ApiResponse.success("Expiring subscriptions retrieved successfully", expiring));
    }

    /**
     * Get expired subscriptions
     */
    @GetMapping("/expired")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<List<SubscriptionResponse>>> getExpiredSubscriptions() {
        log.info("Getting expired subscriptions");
        List<SubscriptionResponse> expired = subscriptionService.getExpiredSubscriptions();
        return ResponseEntity.ok(ApiResponse.success("Expired subscriptions retrieved successfully", expired));
    }

    /**
     * Process expired subscriptions (batch job endpoint)
     */
    @PostMapping("/process-expired")
    @PreAuthorize("hasRole('PLATFORM_OWNER')")
    public ResponseEntity<ApiResponse<Object>> processExpiredSubscriptions() {
        log.info("Processing expired subscriptions");
        Object result = subscriptionService.processExpiredSubscriptions();
        return ResponseEntity.ok(ApiResponse.success("Expired subscriptions processed successfully", result));
    }

    /**
     * Get subscription usage statistics
     */
    @GetMapping("/{id}/usage")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN') or @subscriptionService.canAccessSubscription(#id)")
    public ResponseEntity<ApiResponse<Object>> getSubscriptionUsage(@PathVariable UUID id) {
        log.info("Getting usage statistics for subscription: {}", id);
        Object usage = subscriptionService.getSubscriptionUsage(id);
        return ResponseEntity.ok(ApiResponse.success("Subscription usage retrieved successfully", usage));
    }

    /**
     * Bulk operations on subscriptions
     */
    @PostMapping("/bulk/{action}")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<Object>> bulkOperations(
            @PathVariable String action,
            @RequestBody List<UUID> subscriptionIds,
            @RequestParam(required = false) String reason) {
        log.info("Performing bulk {} on {} subscriptions", action, subscriptionIds.size());
        Object result = subscriptionService.bulkOperations(action, subscriptionIds, reason);
        return ResponseEntity.ok(ApiResponse.success("Bulk operation completed successfully", result));
    }

    /**
     * Get business subscription analytics
     */
    @GetMapping("/business/{businessId}/analytics")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN') or @securityUtils.hasBusinessAccess(#businessId)")
    public ResponseEntity<ApiResponse<Object>> getBusinessSubscriptionAnalytics(@PathVariable UUID businessId) {
        log.info("Getting subscription analytics for business: {}", businessId);
        Object analytics = subscriptionService.getBusinessSubscriptionAnalytics(businessId);
        return ResponseEntity.ok(ApiResponse.success("Business subscription analytics retrieved successfully", analytics));
    }
}

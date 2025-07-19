package com.emenu.features.services.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Subscriptions", description = "User subscription management")
public class UserSubscriptionController {

    private final UserSubscriptionService userSubscriptionService;

    @PostMapping
    @Operation(summary = "Create user subscription")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> createSubscription(
            @Valid @RequestBody CreateSubscriptionRequest request) {
        log.info("Creating subscription for user: {}", request.getUserId());
        SubscriptionResponse response = userSubscriptionService.createSubscription(request);
        return ResponseEntity.ok(ApiResponse.success("Subscription created successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get subscription by ID")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN') or @subscriptionSecurityService.canAccessSubscription(#id)")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> getSubscription(@PathVariable UUID id) {
        log.info("Getting subscription: {}", id);
        SubscriptionResponse response = userSubscriptionService.getSubscription(id);
        return ResponseEntity.ok(ApiResponse.success("Subscription retrieved successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update subscription")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> updateSubscription(
            @PathVariable UUID id, @Valid @RequestBody UpdateSubscriptionRequest request) {
        log.info("Updating subscription: {}", id);
        SubscriptionResponse response = userSubscriptionService.updateSubscription(id, request);
        return ResponseEntity.ok(ApiResponse.success("Subscription updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel subscription")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN') or @subscriptionSecurityService.canAccessSubscription(#id)")
    public ResponseEntity<ApiResponse<Void>> cancelSubscription(@PathVariable UUID id) {
        log.info("Cancelling subscription: {}", id);
        userSubscriptionService.cancelSubscription(id);
        return ResponseEntity.ok(ApiResponse.success("Subscription cancelled successfully", null));
    }

    @GetMapping
    @Operation(summary = "List subscriptions")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<PaginationResponse<SubscriptionResponse>>> listSubscriptions(
            @ModelAttribute SubscriptionFilterRequest filter) {
        log.info("Listing subscriptions");
        PaginationResponse<SubscriptionResponse> response = userSubscriptionService.listSubscriptions(filter);
        return ResponseEntity.ok(ApiResponse.success("Subscriptions retrieved successfully", response));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user's current subscription")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN') or @securityUtils.isCurrentUser(#userId)")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> getUserSubscription(@PathVariable UUID userId) {
        log.info("Getting subscription for user: {}", userId);
        SubscriptionResponse response = userSubscriptionService.getUserSubscription(userId);
        return ResponseEntity.ok(ApiResponse.success("User subscription retrieved successfully", response));
    }

    @PostMapping("/{id}/renew")
    @Operation(summary = "Renew subscription")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN') or @subscriptionSecurityService.canAccessSubscription(#id)")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> renewSubscription(@PathVariable UUID id) {
        log.info("Renewing subscription: {}", id);
        SubscriptionResponse response = userSubscriptionService.renewSubscription(id);
        return ResponseEntity.ok(ApiResponse.success("Subscription renewed successfully", response));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user's subscription")
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> getMySubscription() {
        log.info("Getting current user's subscription");
        SubscriptionResponse response = userSubscriptionService.getCurrentUserSubscription();
        return ResponseEntity.ok(ApiResponse.success("Subscription retrieved successfully", response));
    }
}
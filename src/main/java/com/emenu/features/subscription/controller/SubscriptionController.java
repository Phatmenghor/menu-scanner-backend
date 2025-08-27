package com.emenu.features.subscription.controller;

import com.emenu.features.subscription.dto.filter.SubscriptionFilterRequest;
import com.emenu.features.subscription.dto.request.SubscriptionCancelRequest;
import com.emenu.features.subscription.dto.request.SubscriptionCreateRequest;
import com.emenu.features.subscription.dto.response.SubscriptionCancellationResponse;
import com.emenu.features.subscription.dto.response.SubscriptionRenewalResponse;
import com.emenu.features.subscription.dto.response.SubscriptionResponse;
import com.emenu.features.subscription.dto.request.SubscriptionRenewRequest;
import com.emenu.features.subscription.dto.update.SubscriptionUpdateRequest;
import com.emenu.features.subscription.service.SubscriptionService;
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
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
@Slf4j
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    /**
     * Get all subscriptions with filtering and pagination
     */
    @PostMapping("/all")
    public ResponseEntity<ApiResponse<PaginationResponse<SubscriptionResponse>>> getAllSubscriptions(
            @Valid @RequestBody SubscriptionFilterRequest filter) {
        log.info("Getting subscriptions with filter");
        PaginationResponse<SubscriptionResponse> subscriptions = subscriptionService.getSubscriptions(filter);
        return ResponseEntity.ok(ApiResponse.success("Subscriptions retrieved successfully", subscriptions));
    }

    /**
     * Get current user's business subscriptions
     */
    @PostMapping("/my-business")
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
    public ResponseEntity<ApiResponse<SubscriptionResponse>> getSubscriptionById(@PathVariable UUID id) {
        log.info("Getting subscription by ID: {}", id);
        SubscriptionResponse subscription = subscriptionService.getSubscriptionById(id);
        return ResponseEntity.ok(ApiResponse.success("Subscription retrieved successfully", subscription));
    }

    /**
     * Update subscription
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> updateSubscription(
            @PathVariable UUID id,
            @Valid @RequestBody SubscriptionUpdateRequest request) {
        log.info("Updating subscription: {}", id);
        SubscriptionResponse subscription = subscriptionService.updateSubscription(id, request);
        return ResponseEntity.ok(ApiResponse.success("Subscription updated successfully", subscription));
    }

    /**
     * Delete subscription (now returns SubscriptionResponse)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> deleteSubscription(@PathVariable UUID id) {
        log.info("Deleting subscription: {}", id);
        SubscriptionResponse subscription = subscriptionService.deleteSubscription(id);
        return ResponseEntity.ok(ApiResponse.success("Subscription deleted successfully", subscription));
    }

    /**
     * Renew subscription (now uses request body)
     */
    @PostMapping("/{id}/renew")
    public ResponseEntity<ApiResponse<SubscriptionRenewalResponse>> renewSubscription(
            @PathVariable UUID id,
            @Valid @RequestBody SubscriptionRenewRequest request) {

        log.info("Renewing subscription: {} with payment creation: {}", id, request.shouldCreatePayment());

        SubscriptionResponse subscription = subscriptionService.renewSubscription(id, request);

        SubscriptionRenewalResponse response = new SubscriptionRenewalResponse();
        response.setSubscription(subscription);
        response.setPaymentCreated(request.shouldCreatePayment());

        if (request.shouldCreatePayment()) {
            response.setPaymentAmount(request.getPaymentAmount());
            response.setPaymentMethod(request.getPaymentMethod());
        }

        String message = request.shouldCreatePayment() ?
                "Subscription renewed successfully with payment record" :
                "Subscription renewed successfully";

        return ResponseEntity.ok(ApiResponse.success(message, response));
    }


    /**
     * Cancel subscription (now uses request body and handles payments automatically)
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<SubscriptionCancellationResponse>> cancelSubscription(
            @PathVariable UUID id,
            @Valid @RequestBody SubscriptionCancelRequest request) {

        log.info("Cancelling subscription: {} with refund amount: {}", id, request.getRefundAmount());

        // ✅ Call enhanced service method
        SubscriptionResponse subscription = subscriptionService.cancelSubscription(id, request);

        // ✅ Create comprehensive response
        SubscriptionCancellationResponse response = new SubscriptionCancellationResponse();
        response.setSubscription(subscription);
        response.setPaymentsCleared(true); // Always clear payments
        response.setRefundCreated(request.hasRefundAmount());

        if (request.hasRefundAmount()) {
            response.setRefundAmount(request.getRefundAmount());
        }

        String message = "Subscription cancelled successfully";
        if (request.hasRefundAmount()) {
            message += " with refund record";
        }

        return ResponseEntity.ok(ApiResponse.success(message, response));
    }

}
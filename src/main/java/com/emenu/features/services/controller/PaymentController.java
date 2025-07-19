package com.emenu.features.services.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payments", description = "Payment management")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @Operation(summary = "Create payment")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN', 'BUSINESS_OWNER')")
    public ResponseEntity<ApiResponse<PaymentResponse>> createPayment(
            @Valid @RequestBody CreatePaymentRequest request) {
        log.info("Creating payment for subscription: {}", request.getSubscriptionId());
        PaymentResponse response = paymentService.createPayment(request);
        return ResponseEntity.ok(ApiResponse.success("Payment created successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get payment by ID")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN') or @paymentSecurityService.canAccessPayment(#id)")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPayment(@PathVariable UUID id) {
        log.info("Getting payment: {}", id);
        PaymentResponse response = paymentService.getPayment(id);
        return ResponseEntity.ok(ApiResponse.success("Payment retrieved successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update payment")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<PaymentResponse>> updatePayment(
            @PathVariable UUID id, @Valid @RequestBody UpdatePaymentRequest request) {
        log.info("Updating payment: {}", id);
        PaymentResponse response = paymentService.updatePayment(id, request);
        return ResponseEntity.ok(ApiResponse.success("Payment updated successfully", response));
    }

    @GetMapping
    @Operation(summary = "List payments")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<PaginationResponse<PaymentResponse>>> listPayments(
            @ModelAttribute PaymentFilterRequest filter) {
        log.info("Listing payments");
        PaginationResponse<PaymentResponse> response = paymentService.listPayments(filter);
        return ResponseEntity.ok(ApiResponse.success("Payments retrieved successfully", response));
    }

    @GetMapping("/subscription/{subscriptionId}")
    @Operation(summary = "Get payments for subscription")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN') or @subscriptionSecurityService.canAccessSubscription(#subscriptionId)")
    public ResponseEntity<ApiResponse<PaginationResponse<PaymentResponse>>> getSubscriptionPayments(
            @PathVariable UUID subscriptionId, @ModelAttribute PaymentFilterRequest filter) {
        log.info("Getting payments for subscription: {}", subscriptionId);
        filter.setSubscriptionId(subscriptionId);
        PaginationResponse<PaymentResponse> response = paymentService.listPayments(filter);
        return ResponseEntity.ok(ApiResponse.success("Subscription payments retrieved successfully", response));
    }

    @PostMapping("/{id}/refund")
    @Operation(summary = "Refund payment")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<PaymentResponse>> refundPayment(@PathVariable UUID id) {
        log.info("Refunding payment: {}", id);
        PaymentResponse response = paymentService.refundPayment(id);
        return ResponseEntity.ok(ApiResponse.success("Payment refunded successfully", response));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user's payments")
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public ResponseEntity<ApiResponse<PaginationResponse<PaymentResponse>>> getMyPayments(
            @ModelAttribute PaymentFilterRequest filter) {
        log.info("Getting current user's payments");
        PaginationResponse<PaymentResponse> response = paymentService.getCurrentUserPayments(filter);
        return ResponseEntity.ok(ApiResponse.success("Payments retrieved successfully", response));
    }
}
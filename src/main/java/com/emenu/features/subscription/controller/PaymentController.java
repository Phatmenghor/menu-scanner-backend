package com.emenu.features.subscription.controller;

import com.emenu.features.subscription.dto.request.PaymentCreateRequest;
import com.emenu.features.subscription.dto.resposne.PaymentResponse;
import com.emenu.features.subscription.service.PaymentService;
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
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @PreAuthorize("hasRole('PLATFORM_OWNER') or hasRole('BUSINESS_OWNER')")
    public ResponseEntity<ApiResponse<PaymentResponse>> createPayment(
            @Valid @RequestBody PaymentCreateRequest request) {
        log.info("Creating payment for subscription: {}", request.getSubscriptionId());
        PaymentResponse payment = paymentService.createPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment created successfully", payment));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('PLATFORM_OWNER') or hasRole('BUSINESS_OWNER')")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentById(@PathVariable UUID id) {
        log.info("Getting payment by ID: {}", id);
        PaymentResponse payment = paymentService.getPaymentById(id);
        return ResponseEntity.ok(ApiResponse.success("Payment retrieved successfully", payment));
    }

    @GetMapping("/subscription/{subscriptionId}")
    @PreAuthorize("hasRole('PLATFORM_OWNER') or hasRole('BUSINESS_OWNER')")
    public ResponseEntity<ApiResponse<PaginationResponse<PaymentResponse>>> getPaymentsBySubscription(
            @PathVariable UUID subscriptionId,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        log.info("Getting payments for subscription: {}", subscriptionId);
        PaginationResponse<PaymentResponse> payments = paymentService.getPaymentsBySubscription(subscriptionId, pageNo, pageSize);
        return ResponseEntity.ok(ApiResponse.success("Payments retrieved successfully", payments));
    }

    @GetMapping("/overdue")
    @PreAuthorize("hasRole('PLATFORM_OWNER')")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getOverduePayments() {
        log.info("Getting overdue payments");
        List<PaymentResponse> payments = paymentService.getOverduePayments();
        return ResponseEntity.ok(ApiResponse.success("Overdue payments retrieved successfully", payments));
    }

    @PostMapping("/{id}/process")
    @PreAuthorize("hasRole('PLATFORM_OWNER')")
    public ResponseEntity<ApiResponse<PaymentResponse>> processPayment(
            @PathVariable UUID id,
            @RequestParam String transactionId) {
        log.info("Processing payment: {}", id);
        PaymentResponse payment = paymentService.processPayment(id, transactionId);
        return ResponseEntity.ok(ApiResponse.success("Payment processed successfully", payment));
    }

    @PostMapping("/{id}/refund")
    @PreAuthorize("hasRole('PLATFORM_OWNER')")
    public ResponseEntity<ApiResponse<PaymentResponse>> refundPayment(
            @PathVariable UUID id,
            @RequestParam Double amount) {
        log.info("Refunding payment: {}", id);
        PaymentResponse payment = paymentService.refundPayment(id, amount);
        return ResponseEntity.ok(ApiResponse.success("Payment refunded successfully", payment));
    }

    @PostMapping("/{id}/fail")
    @PreAuthorize("hasRole('PLATFORM_OWNER')")
    public ResponseEntity<ApiResponse<Void>> markPaymentAsFailed(
            @PathVariable UUID id,
            @RequestParam String reason) {
        log.info("Marking payment as failed: {}", id);
        paymentService.markPaymentAsFailed(id, reason);
        return ResponseEntity.ok(ApiResponse.success("Payment marked as failed", null));
    }
}
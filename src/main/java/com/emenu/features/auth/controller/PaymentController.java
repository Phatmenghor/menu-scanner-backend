package com.emenu.features.auth.controller;

import com.emenu.features.auth.dto.filter.PaymentFilterRequest;
import com.emenu.features.auth.dto.request.PaymentCreateRequest;
import com.emenu.features.auth.dto.response.PaymentResponse;
import com.emenu.features.auth.dto.update.PaymentUpdateRequest;
import com.emenu.features.auth.service.PaymentService;
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
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentResponse>> createPayment(@Valid @RequestBody PaymentCreateRequest request) {
        log.info("Creating payment for business: {}", request.getBusinessId());
        PaymentResponse payment = paymentService.createPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment created successfully", payment));
    }

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<PaginationResponse<PaymentResponse>>> getAllPayments(@Valid @RequestBody PaymentFilterRequest filter) {
        log.info("Getting all payments with filter");
        PaginationResponse<PaymentResponse> payments = paymentService.getAllPayments(filter);
        return ResponseEntity.ok(ApiResponse.success("Payments retrieved successfully", payments));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentById(@PathVariable UUID id) {
        log.info("Getting payment by ID: {}", id);
        PaymentResponse payment = paymentService.getPaymentById(id);
        return ResponseEntity.ok(ApiResponse.success("Payment retrieved successfully", payment));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentResponse>> updatePayment(@PathVariable UUID id, @Valid @RequestBody PaymentUpdateRequest request) {
        log.info("Updating payment: {}", id);
        PaymentResponse payment = paymentService.updatePayment(id, request);
        return ResponseEntity.ok(ApiResponse.success("Payment updated successfully", payment));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentResponse>> deletePayment(@PathVariable UUID id) {
        log.info("Deleting payment: {}", id);
        PaymentResponse payment = paymentService.deletePayment(id);
        return ResponseEntity.ok(ApiResponse.success("Payment deleted successfully", payment));
    }

    @GetMapping("/business/{businessId}")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getBusinessPayments(@PathVariable UUID businessId) {
        log.info("Getting payments for business: {}", businessId);
        List<PaymentResponse> payments = paymentService.getBusinessPayments(businessId);
        return ResponseEntity.ok(ApiResponse.success("Business payments retrieved successfully", payments));
    }

    @PostMapping("/business/{businessId}/search")
    public ResponseEntity<ApiResponse<PaginationResponse<PaymentResponse>>> searchBusinessPayments(
            @PathVariable UUID businessId, @Valid @RequestBody PaymentFilterRequest filter) {
        log.info("Searching payments for business: {}", businessId);
        PaginationResponse<PaymentResponse> payments = paymentService.getBusinessPaymentsPaginated(businessId, filter);
        return ResponseEntity.ok(ApiResponse.success("Business payments retrieved successfully", payments));
    }

    @GetMapping("/reference/{referenceNumber}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByReference(@PathVariable String referenceNumber) {
        log.info("Getting payment by reference: {}", referenceNumber);
        PaymentResponse payment = paymentService.getPaymentByReference(referenceNumber);
        return ResponseEntity.ok(ApiResponse.success("Payment retrieved successfully", payment));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<PaymentResponse>> completePayment(@PathVariable UUID id, @RequestParam(required = false) String notes) {
        log.info("Completing payment: {}", id);
        PaymentResponse payment = paymentService.completePayment(id, notes);
        return ResponseEntity.ok(ApiResponse.success("Payment completed successfully", payment));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<PaymentResponse>> cancelPayment(@PathVariable UUID id, @RequestParam(required = false) String reason) {
        log.info("Cancelling payment: {}", id);
        PaymentResponse payment = paymentService.cancelPayment(id, reason);
        return ResponseEntity.ok(ApiResponse.success("Payment cancelled successfully", payment));
    }

    @GetMapping("/generate-reference")
    public ResponseEntity<ApiResponse<String>> generateReferenceNumber() {
        String referenceNumber = paymentService.generateReferenceNumber();
        return ResponseEntity.ok(ApiResponse.success("Reference number generated successfully", referenceNumber));
    }

    @GetMapping("/stats/total")
    public ResponseEntity<ApiResponse<Long>> getTotalPaymentsCount() {
        long count = paymentService.getTotalPaymentsCount();
        return ResponseEntity.ok(ApiResponse.success("Total payments count retrieved successfully", count));
    }

    @GetMapping("/stats/completed")
    public ResponseEntity<ApiResponse<Long>> getCompletedPaymentsCount() {
        long count = paymentService.getCompletedPaymentsCount();
        return ResponseEntity.ok(ApiResponse.success("Completed payments count retrieved successfully", count));
    }

    @GetMapping("/stats/pending")
    public ResponseEntity<ApiResponse<Long>> getPendingPaymentsCount() {
        long count = paymentService.getPendingPaymentsCount();
        return ResponseEntity.ok(ApiResponse.success("Pending payments count retrieved successfully", count));
    }
}
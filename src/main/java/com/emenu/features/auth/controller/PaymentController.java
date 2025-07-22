package com.emenu.features.auth.controller;

import com.emenu.features.auth.dto.filter.PaymentFilterRequest;
import com.emenu.features.auth.dto.request.PaymentCreateRequest;
import com.emenu.features.auth.dto.request.PaymentProcessRequest;
import com.emenu.features.auth.dto.response.PaymentResponse;
import com.emenu.features.auth.dto.response.PaymentSummaryResponse;
import com.emenu.features.auth.dto.update.PaymentUpdateRequest;
import com.emenu.features.auth.service.PaymentService;
import com.emenu.shared.dto.ApiResponse;
import com.emenu.shared.dto.PaginationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    // CRUD Operations
    @PostMapping
    public ResponseEntity<ApiResponse<PaymentResponse>> recordPayment(@Valid @RequestBody PaymentCreateRequest request) {
        log.info("Recording payment for business: {} with amount: {}", request.getBusinessId(), request.getAmount());
        PaymentResponse payment = paymentService.recordPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment recorded successfully", payment));
    }

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<PaginationResponse<PaymentResponse>>> getPayments(@Valid @RequestBody PaymentFilterRequest filter) {
        log.info("Getting payments with filter");
        PaginationResponse<PaymentResponse> payments = paymentService.getPayments(filter);
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

    // Payment Processing Operations
    @PostMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<PaymentResponse>> completePayment(@PathVariable UUID id, @Valid @RequestBody PaymentProcessRequest request) {
        log.info("Completing payment: {}", id);
        PaymentResponse payment = paymentService.completePayment(id, request);
        return ResponseEntity.ok(ApiResponse.success("Payment completed successfully", payment));
    }

    @PostMapping("/{id}/fail")
    public ResponseEntity<ApiResponse<PaymentResponse>> failPayment(@PathVariable UUID id, @Valid @RequestBody PaymentProcessRequest request) {
        log.info("Marking payment as failed: {}", id);
        PaymentResponse payment = paymentService.failPayment(id, request);
        return ResponseEntity.ok(ApiResponse.success("Payment marked as failed", payment));
    }

    @PostMapping("/{id}/refund")
    public ResponseEntity<ApiResponse<PaymentResponse>> refundPayment(@PathVariable UUID id, @Valid @RequestBody PaymentProcessRequest request) {
        log.info("Refunding payment: {}", id);
        PaymentResponse payment = paymentService.refundPayment(id, request);
        return ResponseEntity.ok(ApiResponse.success("Payment refunded successfully", payment));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<PaymentResponse>> cancelPayment(@PathVariable UUID id, @Valid @RequestBody PaymentProcessRequest request) {
        log.info("Cancelling payment: {}", id);
        PaymentResponse payment = paymentService.cancelPayment(id, request);
        return ResponseEntity.ok(ApiResponse.success("Payment cancelled successfully", payment));
    }

    // Business Payment Operations
    @GetMapping("/business/{businessId}")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getBusinessPayments(@PathVariable UUID businessId) {
        log.info("Getting payments for business: {}", businessId);
        List<PaymentResponse> payments = paymentService.getBusinessPayments(businessId);
        return ResponseEntity.ok(ApiResponse.success("Business payments retrieved successfully", payments));
    }

    @PostMapping("/business/{businessId}/search")
    public ResponseEntity<ApiResponse<PaginationResponse<PaymentResponse>>> getBusinessPaymentsPaginated(
            @PathVariable UUID businessId, @Valid @RequestBody PaymentFilterRequest filter) {
        log.info("Getting paginated payments for business: {}", businessId);
        PaginationResponse<PaymentResponse> payments = paymentService.getBusinessPaymentsPaginated(businessId, filter);
        return ResponseEntity.ok(ApiResponse.success("Business payments retrieved successfully", payments));
    }

    @GetMapping("/business/{businessId}/total")
    public ResponseEntity<ApiResponse<BigDecimal>> getBusinessTotalPaidAmount(@PathVariable UUID businessId) {
        log.info("Getting total paid amount for business: {}", businessId);
        BigDecimal total = paymentService.getBusinessTotalPaidAmount(businessId);
        return ResponseEntity.ok(ApiResponse.success("Total paid amount retrieved successfully", total));
    }

    @GetMapping("/business/{businessId}/summary")
    public ResponseEntity<ApiResponse<PaymentSummaryResponse>> getBusinessPaymentSummary(@PathVariable UUID businessId) {
        log.info("Getting payment summary for business: {}", businessId);
        PaymentSummaryResponse summary = paymentService.getBusinessPaymentSummary(businessId);
        return ResponseEntity.ok(ApiResponse.success("Business payment summary retrieved successfully", summary));
    }

    // Analytics & Reports
    @GetMapping("/analytics/summary")
    public ResponseEntity<ApiResponse<PaymentSummaryResponse>> getOverallPaymentSummary() {
        log.info("Getting overall payment summary");
        PaymentSummaryResponse summary = paymentService.getOverallPaymentSummary();
        return ResponseEntity.ok(ApiResponse.success("Payment summary retrieved successfully", summary));
    }

    @GetMapping("/analytics/revenue/total")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalRevenue() {
        log.info("Getting total revenue");
        BigDecimal revenue = paymentService.getTotalRevenue();
        return ResponseEntity.ok(ApiResponse.success("Total revenue retrieved successfully", revenue));
    }

    @GetMapping("/analytics/revenue/monthly")
    public ResponseEntity<ApiResponse<BigDecimal>> getMonthlyRevenue() {
        log.info("Getting monthly revenue");
        BigDecimal revenue = paymentService.getMonthlyRevenue();
        return ResponseEntity.ok(ApiResponse.success("Monthly revenue retrieved successfully", revenue));
    }

    @GetMapping("/analytics/revenue/yearly")
    public ResponseEntity<ApiResponse<BigDecimal>> getYearlyRevenue() {
        log.info("Getting yearly revenue");
        BigDecimal revenue = paymentService.getYearlyRevenue();
        return ResponseEntity.ok(ApiResponse.success("Yearly revenue retrieved successfully", revenue));
    }

    @GetMapping("/analytics/counts/pending")
    public ResponseEntity<ApiResponse<Long>> getPendingPaymentsCount() {
        log.info("Getting pending payments count");
        long count = paymentService.getPendingPaymentsCount();
        return ResponseEntity.ok(ApiResponse.success("Pending payments count retrieved successfully", count));
    }

    @GetMapping("/analytics/counts/overdue")
    public ResponseEntity<ApiResponse<Long>> getOverduePaymentsCount() {
        log.info("Getting overdue payments count");
        long count = paymentService.getOverduePaymentsCount();
        return ResponseEntity.ok(ApiResponse.success("Overdue payments count retrieved successfully", count));
    }

    @GetMapping("/overdue")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getOverduePayments() {
        log.info("Getting overdue payments");
        List<PaymentResponse> payments = paymentService.getOverduePayments();
        return ResponseEntity.ok(ApiResponse.success("Overdue payments retrieved successfully", payments));
    }

    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getRecentPayments(@RequestParam(defaultValue = "10") int limit) {
        log.info("Getting recent payments with limit: {}", limit);
        List<PaymentResponse> payments = paymentService.getRecentPayments(limit);
        return ResponseEntity.ok(ApiResponse.success("Recent payments retrieved successfully", payments));
    }

    // Utility Operations
    @GetMapping("/reference/{referenceNumber}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByReferenceNumber(@PathVariable String referenceNumber) {
        log.info("Getting payment by reference number: {}", referenceNumber);
        PaymentResponse payment = paymentService.getPaymentByReferenceNumber(referenceNumber);
        return ResponseEntity.ok(ApiResponse.success("Payment retrieved successfully", payment));
    }

    @GetMapping("/generate-reference")
    public ResponseEntity<ApiResponse<String>> generateReferenceNumber() {
        String referenceNumber = paymentService.generateReferenceNumber();
        return ResponseEntity.ok(ApiResponse.success("Reference number generated successfully", referenceNumber));
    }
}
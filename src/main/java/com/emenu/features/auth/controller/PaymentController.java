package com.emenu.features.auth.controller;

import com.emenu.features.auth.dto.filter.PaymentFilterRequest;
import com.emenu.features.auth.dto.request.PaymentCreateRequest;
import com.emenu.features.auth.dto.response.PaymentResponse;
import com.emenu.features.auth.service.PaymentService;
import com.emenu.shared.dto.ApiResponse;
import com.emenu.shared.dto.PaginationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("/record")
    public ResponseEntity<ApiResponse<PaymentResponse>> recordPayment(@Valid @RequestBody PaymentCreateRequest request) {
        log.info("Recording payment for business: {}", request.getBusinessId());
        PaymentResponse payment = paymentService.recordPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment recorded successfully", payment));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PaginationResponse<PaymentResponse>>> getPayments(@ModelAttribute PaymentFilterRequest filter) {
        log.info("Getting payments with filter");
        // ✅ Service returns pagination response directly from mapper
        PaginationResponse<PaymentResponse> payments = paymentService.getPayments(filter);
        return ResponseEntity.ok(ApiResponse.success("Payments retrieved successfully", payments));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentById(@PathVariable UUID id) {
        log.info("Getting payment by ID: {}", id);
        PaymentResponse payment = paymentService.getPaymentById(id);
        return ResponseEntity.ok(ApiResponse.success("Payment retrieved successfully", payment));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<PaymentResponse>> completePayment(@PathVariable UUID id) {
        log.info("Completing payment: {}", id);
        PaymentResponse payment = paymentService.completePayment(id);
        return ResponseEntity.ok(ApiResponse.success("Payment completed successfully", payment));
    }

    @PostMapping("/{id}/fail")
    public ResponseEntity<ApiResponse<PaymentResponse>> failPayment(@PathVariable UUID id) {
        log.info("Marking payment as failed: {}", id);
        PaymentResponse payment = paymentService.failPayment(id);
        return ResponseEntity.ok(ApiResponse.success("Payment marked as failed", payment));
    }

    @PostMapping("/{id}/refund")
    public ResponseEntity<ApiResponse<PaymentResponse>> refundPayment(@PathVariable UUID id) {
        log.info("Refunding payment: {}", id);
        PaymentResponse payment = paymentService.refundPayment(id);
        return ResponseEntity.ok(ApiResponse.success("Payment refunded successfully", payment));
    }

    @GetMapping("/business/{businessId}")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getBusinessPayments(@PathVariable UUID businessId) {
        log.info("Getting payments for business: {}", businessId);
        List<PaymentResponse> payments = paymentService.getBusinessPayments(businessId);
        return ResponseEntity.ok(ApiResponse.success("Business payments retrieved successfully", payments));
    }

    @GetMapping("/business/{businessId}/total")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalPaidAmount(@PathVariable UUID businessId) {
        log.info("Getting total paid amount for business: {}", businessId);
        BigDecimal total = paymentService.getTotalPaidAmount(businessId);
        return ResponseEntity.ok(ApiResponse.success("Total paid amount retrieved successfully", total));
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

    @GetMapping("/analytics/pending-count")
    public ResponseEntity<ApiResponse<Long>> getPendingPaymentsCount() {
        log.info("Getting pending payments count");
        long count = paymentService.getPendingPaymentsCount();
        return ResponseEntity.ok(ApiResponse.success("Pending payments count retrieved successfully", count));
    }
}
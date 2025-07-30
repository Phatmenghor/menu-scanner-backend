package com.emenu.features.payment.controller;

import com.emenu.features.payment.dto.filter.PaymentFilterRequest;
import com.emenu.features.payment.dto.request.PaymentCreateRequest;
import com.emenu.features.payment.dto.response.PaymentResponse;
import com.emenu.features.payment.dto.update.PaymentUpdateRequest;
import com.emenu.features.payment.service.PaymentService;
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
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentResponse>> createPayment(@Valid @RequestBody PaymentCreateRequest request) {

        // ✅ Determine payment type for logging
        String paymentType = "Unknown";

        if (request.hasSubscriptionInfo()) {
            paymentType = "Subscription Payment";
        } else if (request.hasBusinessInfo()) {
            paymentType = "Business Record";
        }


        PaymentResponse payment = paymentService.createPayment(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(paymentType + " created successfully", payment));
    }


    @PostMapping("/all")
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

    @GetMapping("/generate-reference")
    public ResponseEntity<ApiResponse<String>> generateReferenceNumber() {
        String referenceNumber = paymentService.generateReferenceNumber();
        return ResponseEntity.ok(ApiResponse.success("Reference number generated successfully", referenceNumber));
    }

}
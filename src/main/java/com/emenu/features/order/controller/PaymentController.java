package com.emenu.features.order.controller;

import com.emenu.features.order.dto.filter.PaymentFilterRequest;
import com.emenu.features.order.dto.request.PaymentCreateRequest;
import com.emenu.features.order.dto.response.PaymentResponse;
import com.emenu.features.order.dto.update.PaymentUpdateRequest;
import com.emenu.features.order.service.PaymentService;
import com.emenu.shared.dto.ApiResponse;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.generate.PaymentReferenceGenerator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentReferenceGenerator referenceGenerator;

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentResponse>> createPayment(@Valid @RequestBody PaymentCreateRequest request) {
        log.info("POST /payments - amount: {}", request.getAmount());
        PaymentResponse payment = paymentService.createPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment created successfully", payment));
    }

    @PostMapping("/all")
    public ResponseEntity<ApiResponse<PaginationResponse<PaymentResponse>>> getAllPayments(
            @Valid @RequestBody PaymentFilterRequest filter) {
        log.info("POST /payments/all - page: {}", filter.getPageNo());
        PaginationResponse<PaymentResponse> payments = paymentService.getAllPayments(filter);
        return ResponseEntity.ok(ApiResponse.success("Payments retrieved successfully", payments));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentById(@PathVariable UUID id) {
        log.info("GET /payments/{}", id);
        PaymentResponse payment = paymentService.getPaymentById(id);
        return ResponseEntity.ok(ApiResponse.success("Payment retrieved successfully", payment));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentResponse>> updatePayment(
            @PathVariable UUID id,
            @Valid @RequestBody PaymentUpdateRequest request) {
        log.info("PUT /payments/{}", id);
        PaymentResponse payment = paymentService.updatePayment(id, request);
        return ResponseEntity.ok(ApiResponse.success("Payment updated successfully", payment));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentResponse>> deletePayment(@PathVariable UUID id) {
        log.info("DELETE /payments/{}", id);
        PaymentResponse payment = paymentService.deletePayment(id);
        return ResponseEntity.ok(ApiResponse.success("Payment deleted successfully", payment));
    }

    @GetMapping("/generate-reference")
    public ResponseEntity<Map<String, String>> generateReference() {
        log.info("GET /payments/generate-reference");
        String reference = referenceGenerator.generateUniqueReference();
        return ResponseEntity.ok(Map.of(
                "reference", reference,
                "timestamp", LocalDateTime.now().toString()
        ));
    }
}
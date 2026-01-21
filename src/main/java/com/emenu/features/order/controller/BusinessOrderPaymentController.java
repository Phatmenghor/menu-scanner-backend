package com.emenu.features.order.controller;

import com.emenu.features.auth.models.User;
import com.emenu.features.order.dto.filter.BusinessOrderPaymentFilterRequest;
import com.emenu.features.order.dto.response.BusinessOrderPaymentResponse;
import com.emenu.features.order.service.BusinessOrderPaymentService;
import com.emenu.security.SecurityUtils;
import com.emenu.shared.dto.ApiResponse;
import com.emenu.shared.dto.PaginationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/business-payments")
@RequiredArgsConstructor
@Slf4j
public class BusinessOrderPaymentController {

    private final BusinessOrderPaymentService paymentService;
    private final SecurityUtils securityUtils;

    /**
     * Get all payments with filtering
     */
    @PostMapping("/all")
    public ResponseEntity<ApiResponse<PaginationResponse<BusinessOrderPaymentResponse>>> getAllPayments(@Valid @RequestBody BusinessOrderPaymentFilterRequest filter) {
        log.info("Getting all business payments with filters");
        PaginationResponse<BusinessOrderPaymentResponse> payments = paymentService.getAllPayments(filter);
        return ResponseEntity.ok(ApiResponse.success("Payments retrieved successfully", payments));
    }

    /**
     * Get my business payments
     */
    @PostMapping("/my-business/all")
    public ResponseEntity<ApiResponse<PaginationResponse<BusinessOrderPaymentResponse>>> getMyBusinessPayments(@Valid @RequestBody BusinessOrderPaymentFilterRequest filter) {
        log.info("Getting payments for current user's business");
        User currentUser = securityUtils.getCurrentUser();
        filter.setBusinessId(currentUser.getBusinessId());
        PaginationResponse<BusinessOrderPaymentResponse> payments = paymentService.getAllPayments(filter);
        return ResponseEntity.ok(ApiResponse.success("Business payments retrieved successfully", payments));
    }

    /**
     * Get payment by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BusinessOrderPaymentResponse>> getPaymentById(@PathVariable UUID id) {
        log.info("Getting payment by ID: {}", id);
        BusinessOrderPaymentResponse payment = paymentService.getPaymentById(id);
        return ResponseEntity.ok(ApiResponse.success("Payment retrieved successfully", payment));
    }

    /**
     * Get payment by order ID
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<BusinessOrderPaymentResponse>> getPaymentByOrderId(@PathVariable UUID orderId) {
        log.info("Getting payment for order: {}", orderId);
        BusinessOrderPaymentResponse payment = paymentService.getPaymentByOrderId(orderId);
        return ResponseEntity.ok(ApiResponse.success("Payment retrieved successfully", payment));
    }

    /**
     * Get POS payments only
     */
    @PostMapping("/pos/all")
    public ResponseEntity<ApiResponse<PaginationResponse<BusinessOrderPaymentResponse>>> getPOSPayments(@Valid @RequestBody BusinessOrderPaymentFilterRequest filter) {
        log.info("Getting POS payments");
        filter.setIsPosOrder(true);
        PaginationResponse<BusinessOrderPaymentResponse> payments = paymentService.getAllPayments(filter);
        return ResponseEntity.ok(ApiResponse.success("POS payments retrieved successfully", payments));
    }

    /**
     * Get guest payments only
     */
    @PostMapping("/guest/all")
    public ResponseEntity<ApiResponse<PaginationResponse<BusinessOrderPaymentResponse>>> getGuestPayments(@Valid @RequestBody BusinessOrderPaymentFilterRequest filter) {
        log.info("Getting guest payments");
        filter.setIsGuestOrder(true);
        PaginationResponse<BusinessOrderPaymentResponse> payments = paymentService.getAllPayments(filter);
        return ResponseEntity.ok(ApiResponse.success("Guest payments retrieved successfully", payments));
    }

    /**
     * Get cash payments only
     */
    @PostMapping("/cash/all")
    public ResponseEntity<ApiResponse<PaginationResponse<BusinessOrderPaymentResponse>>> getCashPayments(@Valid @RequestBody BusinessOrderPaymentFilterRequest filter) {
        log.info("Getting cash payments");
        filter.setPaymentMethod(com.emenu.enums.payment.PaymentMethod.CASH);
        PaginationResponse<BusinessOrderPaymentResponse> payments = paymentService.getAllPayments(filter);
        return ResponseEntity.ok(ApiResponse.success("Cash payments retrieved successfully", payments));
    }
}
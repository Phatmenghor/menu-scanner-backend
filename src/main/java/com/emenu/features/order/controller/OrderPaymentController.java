package com.emenu.features.order.controller;

import com.emenu.features.auth.models.User;
import com.emenu.features.order.dto.filter.OrderPaymentFilterRequest;
import com.emenu.features.order.dto.response.OrderPaymentResponse;
import com.emenu.features.order.service.OrderPaymentService;
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
public class OrderPaymentController {

    private final OrderPaymentService paymentService;
    private final SecurityUtils securityUtils;

    /**
     * Get all payments with filtering
     */
    @PostMapping("/all")
    public ResponseEntity<ApiResponse<PaginationResponse<OrderPaymentResponse>>> getAllPayments(@Valid @RequestBody OrderPaymentFilterRequest filter) {
        log.info("Getting all business payments with filters");
        PaginationResponse<OrderPaymentResponse> payments = paymentService.getAllPayments(filter);
        return ResponseEntity.ok(ApiResponse.success("Payments retrieved successfully", payments));
    }

    /**
     * Get my business payments
     */
    @PostMapping("/my-business/all")
    public ResponseEntity<ApiResponse<PaginationResponse<OrderPaymentResponse>>> getMyBusinessPayments(@Valid @RequestBody OrderPaymentFilterRequest filter) {
        log.info("Getting payments for current user's business");
        User currentUser = securityUtils.getCurrentUser();
        filter.setBusinessId(currentUser.getBusinessId());
        PaginationResponse<OrderPaymentResponse> payments = paymentService.getAllPayments(filter);
        return ResponseEntity.ok(ApiResponse.success("Business payments retrieved successfully", payments));
    }

    /**
     * Get payment by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderPaymentResponse>> getPaymentById(@PathVariable UUID id) {
        log.info("Getting payment by ID: {}", id);
        OrderPaymentResponse payment = paymentService.getPaymentById(id);
        return ResponseEntity.ok(ApiResponse.success("Payment retrieved successfully", payment));
    }

    /**
     * Get payment by order ID
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<OrderPaymentResponse>> getPaymentByOrderId(@PathVariable UUID orderId) {
        log.info("Getting payment for order: {}", orderId);
        OrderPaymentResponse payment = paymentService.getPaymentByOrderId(orderId);
        return ResponseEntity.ok(ApiResponse.success("Payment retrieved successfully", payment));
    }

    /**
     * Get cash payments only
     */
    @PostMapping("/cash/all")
    public ResponseEntity<ApiResponse<PaginationResponse<OrderPaymentResponse>>> getCashPayments(@Valid @RequestBody OrderPaymentFilterRequest filter) {
        log.info("Getting cash payments");
        filter.setPaymentMethod(com.emenu.enums.payment.PaymentMethod.CASH);
        PaginationResponse<OrderPaymentResponse> payments = paymentService.getAllPayments(filter);
        return ResponseEntity.ok(ApiResponse.success("Cash payments retrieved successfully", payments));
    }
}

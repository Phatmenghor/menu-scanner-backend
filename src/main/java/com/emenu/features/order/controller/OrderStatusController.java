package com.emenu.features.order.controller;

import com.emenu.features.order.dto.filter.OrderProcessStatusFilterRequest;
import com.emenu.features.order.dto.request.OrderProcessStatusCreateRequest;
import com.emenu.features.order.dto.response.OrderProcessStatusResponse;
import com.emenu.features.order.dto.update.OrderProcessStatusUpdateRequest;
import com.emenu.features.order.service.OrderProcessStatusService;
import com.emenu.security.SecurityUtils;
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
@RequestMapping("/api/v1/order-statuses")
@RequiredArgsConstructor
@Slf4j
public class OrderStatusController {

    private final OrderProcessStatusService orderProcessStatusService;
    private final SecurityUtils securityUtils;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderProcessStatusResponse>> createOrderStatus(
            @Valid @RequestBody OrderProcessStatusCreateRequest request) {
        log.info("Creating order status: {}", request.getName());
        OrderProcessStatusResponse response = orderProcessStatusService.createOrderProcessStatus(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order status created successfully", response));
    }

    @PostMapping("/all")
    public ResponseEntity<ApiResponse<PaginationResponse<OrderProcessStatusResponse>>> getAllOrderStatuses(
            @Valid @RequestBody OrderProcessStatusFilterRequest filter) {
        log.info("Getting all order statuses with filters");
        PaginationResponse<OrderProcessStatusResponse> response =
                orderProcessStatusService.getAllOrderProcessStatuses(filter);
        return ResponseEntity.ok(ApiResponse.success("Order statuses retrieved successfully", response));
    }

    @PostMapping("/my-business/all")
    public ResponseEntity<ApiResponse<PaginationResponse<OrderProcessStatusResponse>>> getMyBusinessOrderStatuses(
            @Valid @RequestBody OrderProcessStatusFilterRequest filter) {
        log.info("Getting order statuses for current user's business");
        UUID businessId = securityUtils.getCurrentUser().getBusinessId();
        filter.setBusinessId(businessId);
        PaginationResponse<OrderProcessStatusResponse> response =
                orderProcessStatusService.getAllOrderProcessStatuses(filter);
        return ResponseEntity.ok(ApiResponse.success("Business order statuses retrieved successfully", response));
    }

    @GetMapping("/business/{businessId}")
    public ResponseEntity<ApiResponse<List<OrderProcessStatusResponse>>> getBusinessOrderStatuses(
            @PathVariable UUID businessId) {
        log.info("Getting active order statuses for business: {}", businessId);
        List<OrderProcessStatusResponse> response =
                orderProcessStatusService.getBusinessOrderProcessStatuses(businessId);
        return ResponseEntity.ok(ApiResponse.success("Business order statuses retrieved successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderProcessStatusResponse>> getOrderStatusById(@PathVariable UUID id) {
        log.info("Getting order status by ID: {}", id);
        OrderProcessStatusResponse response = orderProcessStatusService.getOrderProcessStatusById(id);
        return ResponseEntity.ok(ApiResponse.success("Order status retrieved successfully", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderProcessStatusResponse>> updateOrderStatus(
            @PathVariable UUID id,
            @Valid @RequestBody OrderProcessStatusUpdateRequest request) {
        log.info("Updating order status: {}", id);
        OrderProcessStatusResponse response = orderProcessStatusService.updateOrderProcessStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Order status updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderProcessStatusResponse>> deleteOrderStatus(@PathVariable UUID id) {
        log.info("Deleting order status: {}", id);
        OrderProcessStatusResponse response = orderProcessStatusService.deleteOrderProcessStatus(id);
        return ResponseEntity.ok(ApiResponse.success("Order status deleted successfully", response));
    }
}

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
@RequestMapping("/api/v1/order-process-statuses")
@RequiredArgsConstructor
@Slf4j
public class OrderProcessStatusController {

    private final OrderProcessStatusService orderProcessStatusService;
    private final SecurityUtils securityUtils;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderProcessStatusResponse>> createOrderProcessStatus(
            @Valid @RequestBody OrderProcessStatusCreateRequest request) {
        log.info("Creating order process status: {}", request.getName());
        OrderProcessStatusResponse response = orderProcessStatusService.createOrderProcessStatus(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order process status created successfully", response));
    }

    @PostMapping("/all")
    public ResponseEntity<ApiResponse<PaginationResponse<OrderProcessStatusResponse>>> getAllOrderProcessStatuses(
            @Valid @RequestBody OrderProcessStatusFilterRequest filter) {
        log.info("Getting all order process statuses with filters");
        PaginationResponse<OrderProcessStatusResponse> response =
                orderProcessStatusService.getAllOrderProcessStatuses(filter);
        return ResponseEntity.ok(ApiResponse.success("Order process statuses retrieved successfully", response));
    }

    @PostMapping("/my-business/all")
    public ResponseEntity<ApiResponse<PaginationResponse<OrderProcessStatusResponse>>> getMyBusinessOrderProcessStatuses(
            @Valid @RequestBody OrderProcessStatusFilterRequest filter) {
        log.info("Getting order process statuses for current user's business");
        UUID businessId = securityUtils.getCurrentUser().getBusinessId();
        filter.setBusinessId(businessId);
        PaginationResponse<OrderProcessStatusResponse> response =
                orderProcessStatusService.getAllOrderProcessStatuses(filter);
        return ResponseEntity.ok(ApiResponse.success("Business order process statuses retrieved successfully", response));
    }

    @GetMapping("/business/{businessId}")
    public ResponseEntity<ApiResponse<List<OrderProcessStatusResponse>>> getBusinessOrderProcessStatuses(
            @PathVariable UUID businessId) {
        log.info("Getting active order process statuses for business: {}", businessId);
        List<OrderProcessStatusResponse> response =
                orderProcessStatusService.getBusinessOrderProcessStatuses(businessId);
        return ResponseEntity.ok(ApiResponse.success("Business order process statuses retrieved successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderProcessStatusResponse>> getOrderProcessStatusById(@PathVariable UUID id) {
        log.info("Getting order process status by ID: {}", id);
        OrderProcessStatusResponse response = orderProcessStatusService.getOrderProcessStatusById(id);
        return ResponseEntity.ok(ApiResponse.success("Order process status retrieved successfully", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderProcessStatusResponse>> updateOrderProcessStatus(
            @PathVariable UUID id,
            @Valid @RequestBody OrderProcessStatusUpdateRequest request) {
        log.info("Updating order process status: {}", id);
        OrderProcessStatusResponse response = orderProcessStatusService.updateOrderProcessStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Order process status updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderProcessStatusResponse>> deleteOrderProcessStatus(@PathVariable UUID id) {
        log.info("Deleting order process status: {}", id);
        OrderProcessStatusResponse response = orderProcessStatusService.deleteOrderProcessStatus(id);
        return ResponseEntity.ok(ApiResponse.success("Order process status deleted successfully", response));
    }
}

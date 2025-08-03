package com.emenu.features.order.controller;

import com.emenu.features.auth.models.User;
import com.emenu.features.order.dto.request.OrderCreateRequest;
import com.emenu.features.order.dto.response.OrderResponse;
import com.emenu.features.order.dto.update.OrderStatusUpdateRequest;
import com.emenu.features.order.service.OrderService;
import com.emenu.security.SecurityUtils;
import com.emenu.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;
    private final SecurityUtils securityUtils;

    /**
     * Create order from cart (checkout)
     */
    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrderFromCart(@Valid @RequestBody OrderCreateRequest request) {
        log.info("Creating order from cart for business: {}", request.getBusinessId());
        OrderResponse order = orderService.createOrderFromCart(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order created successfully", order));
    }

    /**
     * Get customer order history
     */
    @GetMapping("/my-orders")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getMyOrders() {
        log.info("Getting order history for current customer");
        List<OrderResponse> orders = orderService.getCustomerOrderHistory();
        return ResponseEntity.ok(ApiResponse.success("Order history retrieved successfully", orders));
    }

    /**
     * Get order by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(@PathVariable UUID id) {
        log.info("Getting order by ID: {}", id);
        OrderResponse order = orderService.getOrderById(id);
        return ResponseEntity.ok(ApiResponse.success("Order retrieved successfully", order));
    }

    /**
     * Get business orders (for business owners)
     */
    @GetMapping("/business/{businessId}")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getBusinessOrders(@PathVariable UUID businessId) {
        log.info("Getting orders for business: {}", businessId);
        List<OrderResponse> orders = orderService.getBusinessOrders(businessId);
        return ResponseEntity.ok(ApiResponse.success("Business orders retrieved successfully", orders));
    }

    /**
     * Get current user's business orders
     */
    @GetMapping("/my-business")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getMyBusinessOrders() {
        log.info("Getting orders for current user's business");
        User currentUser = securityUtils.getCurrentUser();
        List<OrderResponse> orders = orderService.getBusinessOrders(currentUser.getBusinessId());
        return ResponseEntity.ok(ApiResponse.success("Business orders retrieved successfully", orders));
    }

    /**
     * Update order status (business only)
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable UUID id,
            @Valid @RequestBody OrderStatusUpdateRequest request) {
        log.info("Updating order status: {} -> {}", id, request.getStatus());
        OrderResponse order = orderService.updateOrderStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Order status updated successfully", order));
    }
}
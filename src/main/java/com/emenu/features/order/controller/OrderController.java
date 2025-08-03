package com.emenu.features.order.controller;

import com.emenu.features.auth.models.User;
import com.emenu.features.order.dto.filter.OrderFilterRequest;
import com.emenu.features.order.dto.request.OrderCreateRequest;
import com.emenu.features.order.dto.request.POSOrderCreateRequest;
import com.emenu.features.order.dto.response.OrderResponse;
import com.emenu.features.order.dto.update.OrderStatusUpdateRequest;
import com.emenu.features.order.service.OrderService;
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
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;
    private final SecurityUtils securityUtils;

    /**
     * Create order from cart (checkout) - Requires login
     */
    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrderFromCart(@Valid @RequestBody OrderCreateRequest request) {
        log.info("Creating order from cart for business: {}", request.getBusinessId());
        OrderResponse order = orderService.createOrderFromCart(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order created successfully", order));
    }

    /**
     * Create guest order (no login required) - Just provide phone number
     */
    @PostMapping("/guest/checkout")
    public ResponseEntity<ApiResponse<OrderResponse>> createGuestOrder(
            @Valid @RequestBody OrderCreateRequest request,
            @RequestParam(required = false) List<UUID> cartItemIds) {
        log.info("Creating guest order for business: {}", request.getBusinessId());
        
        // Mark as guest order
        request.setIsGuestOrder(true);
        
        OrderResponse order = orderService.createGuestOrder(request, cartItemIds);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Guest order created successfully", order));
    }

    /**
     * Create POS order (for business staff) - Like cash register
     */
    @PostMapping("/pos")
    public ResponseEntity<ApiResponse<OrderResponse>> createPOSOrder(@Valid @RequestBody POSOrderCreateRequest request) {
        log.info("Creating POS order for customer: {}", request.getCustomerPhone());
        OrderResponse order = orderService.createPOSOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("POS order created successfully", order));
    }

    /**
     * Get all orders with filtering (Admin/Business view)
     */
    @PostMapping("/all")
    public ResponseEntity<ApiResponse<PaginationResponse<OrderResponse>>> getAllOrders(@Valid @RequestBody OrderFilterRequest filter) {
        log.info("Getting all orders with filters");
        PaginationResponse<OrderResponse> orders = orderService.getAllOrders(filter);
        return ResponseEntity.ok(ApiResponse.success("Orders retrieved successfully", orders));
    }

    /**
     * Get my business orders (for business owners)
     */
    @PostMapping("/my-business/all")
    public ResponseEntity<ApiResponse<PaginationResponse<OrderResponse>>> getMyBusinessOrders(@Valid @RequestBody OrderFilterRequest filter) {
        log.info("Getting orders for current user's business");
        User currentUser = securityUtils.getCurrentUser();
        filter.setBusinessId(currentUser.getBusinessId());
        PaginationResponse<OrderResponse> orders = orderService.getAllOrders(filter);
        return ResponseEntity.ok(ApiResponse.success("Business orders retrieved successfully", orders));
    }

    /**
     * Get customer order history (requires login)
     */
    @GetMapping("/my-orders")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getMyOrders() {
        log.info("Getting order history for current customer");
        List<OrderResponse> orders = orderService.getCustomerOrderHistory();
        return ResponseEntity.ok(ApiResponse.success("Order history retrieved successfully", orders));
    }

    /**
     * Get guest orders by phone number (no login required)
     */
    @GetMapping("/guest/{phone}")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getGuestOrdersByPhone(@PathVariable String phone) {
        log.info("Getting guest orders for phone: {}", phone);
        List<OrderResponse> orders = orderService.getGuestOrdersByPhone(phone);
        return ResponseEntity.ok(ApiResponse.success("Guest orders retrieved successfully", orders));
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

    /**
     * Get business orders by status (quick filters)
     */
    @GetMapping("/business/{businessId}/status/{status}")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getBusinessOrdersByStatus(
            @PathVariable UUID businessId,
            @PathVariable String status) {
        log.info("Getting orders for business: {} with status: {}", businessId, status);
        
        OrderFilterRequest filter = new OrderFilterRequest();
        filter.setBusinessId(businessId);
        // Parse status and set filter
        
        PaginationResponse<OrderResponse> orders = orderService.getAllOrders(filter);
        return ResponseEntity.ok(ApiResponse.success("Orders retrieved successfully", orders.getContent()));
    }
}
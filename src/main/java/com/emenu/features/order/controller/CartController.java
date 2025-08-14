package com.emenu.features.order.controller;

import com.emenu.features.auth.models.User;
import com.emenu.features.order.dto.filter.CartFilterRequest;
import com.emenu.features.order.dto.request.CartItemRequest;
import com.emenu.features.order.dto.response.CartResponse;
import com.emenu.features.order.dto.update.CartUpdateRequest;
import com.emenu.features.order.service.CartService;
import com.emenu.security.SecurityUtils;
import com.emenu.shared.dto.ApiResponse;
import com.emenu.shared.dto.PaginationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final CartService cartService;
    private final SecurityUtils securityUtils;

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<CartResponse>> addOrUpdateCartItem(@Valid @RequestBody CartItemRequest request) {
        log.info("Adding/Updating item in cart - Product: {}, Quantity: {}", request.getProductId(), request.getQuantity());
        CartResponse cart = cartService.addToCart(request);

        String message = request.getQuantity() == 0 ?
                "Item removed from cart successfully" :
                "Item added/updated in cart successfully";

        return ResponseEntity.ok(ApiResponse.success(message, cart));
    }

    /**
     * Update cart item quantity by cart item ID
     */
    @PutMapping("/update")
    public ResponseEntity<ApiResponse<CartResponse>> updateCartItem(@Valid @RequestBody CartUpdateRequest request) {
        log.info("Updating cart item by ID: {}", request.getCartItemId());
        CartResponse cart = cartService.updateCartItem(request);
        return ResponseEntity.ok(ApiResponse.success("Cart item updated successfully", cart));
    }

    /**
     * Remove item from cart by cart item ID
     */
    @DeleteMapping("/item/{cartItemId}")
    public ResponseEntity<ApiResponse<CartResponse>> removeFromCart(@PathVariable UUID cartItemId) {
        log.info("Removing item from cart: {}", cartItemId);
        CartResponse cart = cartService.removeFromCart(cartItemId);
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart successfully", cart));
    }

    /**
     * Get all carts with filtering and pagination (Admin/Business view)
     */
    @PostMapping("/all")
    public ResponseEntity<ApiResponse<PaginationResponse<CartResponse>>> getAllCarts(@Valid @RequestBody CartFilterRequest filter) {
        log.info("Getting all carts with filters");
        PaginationResponse<CartResponse> carts = cartService.getAllCarts(filter);
        return ResponseEntity.ok(ApiResponse.success("Carts retrieved successfully", carts));
    }

    /**
     * Get my carts with filtering and pagination
     * - Customer: Get all my carts across businesses
     * - Business: Get carts for my business
     */
    @PostMapping("/my-carts/all")
    public ResponseEntity<ApiResponse<PaginationResponse<CartResponse>>> getMyCarts(@Valid @RequestBody CartFilterRequest filter) {
        log.info("Getting my carts for current user");
        User currentUser = securityUtils.getCurrentUser();

        if (currentUser.isBusinessUser()) {
            // Business user: filter by their business
            filter.setBusinessId(currentUser.getBusinessId());
        } else {
            // Customer: filter by their user ID
            filter.setUserId(currentUser.getId());
        }

        PaginationResponse<CartResponse> carts = cartService.getMyCarts(filter);
        return ResponseEntity.ok(ApiResponse.success("My carts retrieved successfully", carts));
    }

    @PostMapping("/my-carts/count")
    public ResponseEntity<ApiResponse<Long>> getMyCartItemsCount(@Valid @RequestBody CartFilterRequest filter) {
        log.info("Getting cart items count for current user");
        User currentUser = securityUtils.getCurrentUser();

        filter.setUserId(currentUser.getId());

        Long itemsCount = cartService.getMyCartItemsCount(filter);
        return ResponseEntity.ok(ApiResponse.success("Cart items count retrieved successfully", itemsCount));
    }

}
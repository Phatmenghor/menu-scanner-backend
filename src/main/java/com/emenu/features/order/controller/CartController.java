package com.emenu.features.order.controller;

import com.emenu.features.order.dto.request.CartItemRequest;
import com.emenu.features.order.dto.response.CartResponse;
import com.emenu.features.order.dto.update.CartUpdateRequest;
import com.emenu.features.order.service.CartService;
import com.emenu.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final CartService cartService;

    /**
     * Add/Update item in cart
     * - If item exists: sets exact quantity (quantity = 0 removes item)
     * - If item doesn't exist: creates new item (only if quantity > 0)
     * Perfect for product detail pages where you set exact quantities
     */
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
     * Legacy method - use addOrUpdateCartItem for new implementations
     */
    @PutMapping("/update")
    public ResponseEntity<ApiResponse<CartResponse>> updateCartItem(@Valid @RequestBody CartUpdateRequest request) {
        log.info("Updating cart item by ID: {}", request.getCartItemId());
        CartResponse cart = cartService.updateCartItem(request);
        return ResponseEntity.ok(ApiResponse.success("Cart item updated successfully", cart));
    }

    /**
     * Remove item from cart by cart item ID
     * Legacy method - use addOrUpdateCartItem with quantity=0 for new implementations
     */
    @DeleteMapping("/item/{cartItemId}")
    public ResponseEntity<ApiResponse<CartResponse>> removeFromCart(@PathVariable UUID cartItemId) {
        log.info("Removing item from cart: {}", cartItemId);
        CartResponse cart = cartService.removeFromCart(cartItemId);
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart successfully", cart));
    }

    /**
     * Get current user's cart for specific business
     */
    @GetMapping("/business/{businessId}")
    public ResponseEntity<ApiResponse<CartResponse>> getCart(@PathVariable UUID businessId) {
        log.info("Getting cart for business: {}", businessId);
        CartResponse cart = cartService.getCart(businessId);
        return ResponseEntity.ok(ApiResponse.success("Cart retrieved successfully", cart));
    }

    /**
     * Clear entire cart for business
     */
    @DeleteMapping("/business/{businessId}")
    public ResponseEntity<ApiResponse<CartResponse>> clearCart(@PathVariable UUID businessId) {
        log.info("Clearing cart for business: {}", businessId);
        CartResponse cart = cartService.clearCart(businessId);
        return ResponseEntity.ok(ApiResponse.success("Cart cleared successfully", cart));
    }
}
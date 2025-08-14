package com.emenu.features.order.controller;

import com.emenu.features.auth.models.User;
import com.emenu.features.order.dto.request.CartItemRequest;
import com.emenu.features.order.dto.response.CartResponse;
import com.emenu.features.order.dto.update.CartUpdateRequest;
import com.emenu.features.order.service.CartService;
import com.emenu.security.SecurityUtils;
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
    private final SecurityUtils securityUtils;

    /**
     * GET - Get cart by business ID
     * User ID from JWT token, Business ID from request param
     */
    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(@RequestParam UUID businessId) {
        log.info("Getting cart for business: {}", businessId);
        
        User currentUser = securityUtils.getCurrentUser();
        CartResponse cart = cartService.getCart(currentUser.getId(), businessId);
        
        return ResponseEntity.ok(ApiResponse.success("Cart retrieved successfully", cart));
    }

    /**
     * POST - Add item to cart
     */
    @PostMapping("/add")
    public ResponseEntity<ApiResponse<CartResponse>> addToCart(
            @RequestParam UUID businessId,
            @Valid @RequestBody CartItemRequest request) {
        
        log.info("Adding item to cart - Product: {}, Business: {}, Quantity: {}", 
                request.getProductId(), businessId, request.getQuantity());
        
        User currentUser = securityUtils.getCurrentUser();
        CartResponse cart = cartService.addToCart(currentUser.getId(), businessId, request);
        
        String message = request.getQuantity() == 0 ? 
                "Item removed from cart successfully" : 
                "Item added to cart successfully";
        
        return ResponseEntity.ok(ApiResponse.success(message, cart));
    }

    /**
     * PUT - Update cart item quantity
     */
    @PutMapping("/update")
    public ResponseEntity<ApiResponse<CartResponse>> updateCartItem(
            @RequestParam UUID businessId,
            @Valid @RequestBody CartUpdateRequest request) {
        
        log.info("Updating cart item: {} for business: {}", request.getCartItemId(), businessId);
        
        User currentUser = securityUtils.getCurrentUser();
        CartResponse cart = cartService.updateCartItem(currentUser.getId(), businessId, request);
        
        return ResponseEntity.ok(ApiResponse.success("Cart item updated successfully", cart));
    }

    /**
     * DELETE - Remove specific item from cart
     */
    @DeleteMapping("/item/{cartItemId}")
    public ResponseEntity<ApiResponse<CartResponse>> removeFromCart(
            @PathVariable UUID cartItemId,
            @RequestParam UUID businessId) {
        
        log.info("Removing cart item: {} from business: {}", cartItemId, businessId);
        
        User currentUser = securityUtils.getCurrentUser();
        CartResponse cart = cartService.removeFromCart(currentUser.getId(), businessId, cartItemId);
        
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart successfully", cart));
    }

    /**
     * DELETE - Clear entire cart for business
     */
    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<CartResponse>> clearCart(@RequestParam UUID businessId) {
        log.info("Clearing cart for business: {}", businessId);
        
        User currentUser = securityUtils.getCurrentUser();
        CartResponse cart = cartService.clearCart(currentUser.getId(), businessId);
        
        return ResponseEntity.ok(ApiResponse.success("Cart cleared successfully", cart));
    }

    /**
     * GET - Get cart items count only
     */
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> getCartItemsCount(@RequestParam UUID businessId) {
        log.info("Getting cart items count for business: {}", businessId);
        
        User currentUser = securityUtils.getCurrentUser();
        Long count = cartService.getCartItemsCount(currentUser.getId(), businessId);
        
        return ResponseEntity.ok(ApiResponse.success("Cart items count retrieved successfully", count));
    }
}
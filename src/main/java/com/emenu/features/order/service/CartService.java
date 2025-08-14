package com.emenu.features.order.service;

import com.emenu.features.order.dto.request.CartItemRequest;
import com.emenu.features.order.dto.response.CartResponse;
import com.emenu.features.order.dto.update.CartUpdateRequest;

import java.util.UUID;

public interface CartService {
    
    /**
     * GET - Get cart by user ID and business ID
     */
    CartResponse getCart(UUID userId, UUID businessId);
    
    /**
     * POST - Add item to cart
     */
    CartResponse addToCart(UUID userId, UUID businessId, CartItemRequest request);
    
    /**
     * PUT - Update cart item quantity
     */
    CartResponse updateCartItem(UUID userId, UUID businessId, CartUpdateRequest request);
    
    /**
     * DELETE - Remove specific item from cart
     */
    CartResponse removeFromCart(UUID userId, UUID businessId, UUID cartItemId);
    
    /**
     * DELETE - Clear entire cart
     */
    CartResponse clearCart(UUID userId, UUID businessId);
    
    /**
     * GET - Get cart items count
     */
    Long getCartItemsCount(UUID userId, UUID businessId);
}
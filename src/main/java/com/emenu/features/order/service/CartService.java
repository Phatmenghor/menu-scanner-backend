package com.emenu.features.order.service;

import com.emenu.features.order.dto.request.CartItemRequest;
import com.emenu.features.order.dto.response.CartResponse;
import com.emenu.features.order.dto.update.CartUpdateRequest;

import java.util.UUID;

public interface CartService {
    
    // Add item to cart
    CartResponse addToCart(CartItemRequest request);
    
    // Update cart item quantity
    CartResponse updateCartItem(CartUpdateRequest request);
    
    // Remove item from cart
    CartResponse removeFromCart(UUID cartItemId);
    
    // Get current user's cart for specific business
    CartResponse getCart(UUID businessId);
    
    // Clear entire cart
    CartResponse clearCart(UUID businessId);
}

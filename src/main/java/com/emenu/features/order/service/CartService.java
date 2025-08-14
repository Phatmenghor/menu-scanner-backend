package com.emenu.features.order.service;

import com.emenu.features.order.dto.filter.CartFilterRequest;
import com.emenu.features.order.dto.request.CartItemRequest;
import com.emenu.features.order.dto.response.CartResponse;
import com.emenu.features.order.dto.update.CartUpdateRequest;
import com.emenu.shared.dto.PaginationResponse;

import java.util.List;
import java.util.UUID;

public interface CartService {
    
    // Cart item operations
    CartResponse addToCart(CartItemRequest request);
    CartResponse updateCartItem(CartUpdateRequest request);
    CartResponse removeFromCart(UUID cartItemId);

    // Multiple carts operations with pagination
    PaginationResponse<CartResponse> getAllCarts(CartFilterRequest filter);

    // My carts with filtering and pagination
    PaginationResponse<CartResponse> getMyCarts(CartFilterRequest filter);
    Long getMyCartItemsCount(CartFilterRequest filter);
}
package com.emenu.features.order.service;

import com.emenu.features.order.dto.request.CartItemCreateRequest;
import com.emenu.features.order.dto.response.CartSummaryResponse;

public interface CartService {

    /**
     * POST - Submit cart item (add/update/remove)
     * Quantity 0 = remove, quantity >= 1 = set quantity
     */
    CartSummaryResponse submitCartItem(CartItemCreateRequest request);

    /**
     * GET - Get current user's cart
     */
    CartSummaryResponse getCart();

    /**
     * DELETE - Clear current user's cart
     */
    CartSummaryResponse clearCart();
}

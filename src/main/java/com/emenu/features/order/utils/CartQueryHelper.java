package com.emenu.features.order.utils;

import com.emenu.features.order.repository.CartItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Helper class for querying cart-related data.
 * Used to efficiently fetch cart quantities for product listings.
 */
@Component
@RequiredArgsConstructor
public class CartQueryHelper {

    private final CartItemRepository cartItemRepository;

    /**
     * Get cart quantities for multiple products for a specific user and business.
     * Returns a map of productId to total quantity in cart.
     *
     * @param userId      The user ID
     * @param businessId  The business ID
     * @param productIds  List of product IDs to check
     * @return Map of productId to total quantity in cart
     */
    public Map<UUID, Integer> getProductQuantitiesInCart(UUID userId, UUID businessId, List<UUID> productIds) {
        if (userId == null || businessId == null || productIds == null || productIds.isEmpty()) {
            return Map.of();
        }

        List<Map<String, Object>> results = cartItemRepository.getProductQuantitiesInCart(userId, businessId, productIds);

        Map<UUID, Integer> quantityMap = new HashMap<>();
        for (Map<String, Object> result : results) {
            UUID productId = (UUID) result.get("productId");
            // Cast to Number first, then convert to int to handle both Integer and Long
            Number totalQuantity = (Number) result.get("totalQuantity");
            quantityMap.put(productId, totalQuantity.intValue());
        }

        return quantityMap;
    }
}

package com.emenu.features.order.utils;

import com.emenu.features.order.dto.CartQuantityProjection;
import com.emenu.features.order.dto.SizeCartQuantityProjection;
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
     * If businessId is null, queries across all businesses.
     *
     * @param userId      The user ID
     * @param businessId  The business ID (nullable)
     * @param productIds  List of product IDs to check
     * @return Map of productId to total quantity in cart
     */
    public Map<UUID, Integer> getProductQuantitiesInCart(UUID userId, UUID businessId, List<UUID> productIds) {
        if (userId == null || productIds == null || productIds.isEmpty()) {
            return Map.of();
        }

        List<CartQuantityProjection> results;
        if (businessId != null) {
            results = cartItemRepository.getProductQuantitiesInCart(userId, businessId, productIds);
        } else {
            results = cartItemRepository.getProductQuantitiesInCartAllBusinesses(userId, productIds);
        }

        Map<UUID, Integer> quantityMap = new HashMap<>();
        for (CartQuantityProjection result : results) {
            UUID productId = result.getProductId();
            Long totalQuantity = result.getTotalQuantity();
            if (productId != null && totalQuantity != null) {
                quantityMap.put(productId, totalQuantity.intValue());
            }
        }

        return quantityMap;
    }

    /**
     * Get per-size quantities for a specific product in user's cart.
     *
     * @param userId    The user ID
     * @param productId The product ID
     * @return Map of productSizeId to quantity in cart
     */
    public Map<UUID, Integer> getSizeQuantitiesInCart(UUID userId, UUID productId) {
        if (userId == null || productId == null) {
            return Map.of();
        }

        List<SizeCartQuantityProjection> results = cartItemRepository.getSizeQuantitiesInCart(userId, productId);

        Map<UUID, Integer> quantityMap = new HashMap<>();
        for (SizeCartQuantityProjection result : results) {
            UUID sizeId = result.getProductSizeId();
            Integer quantity = result.getQuantity();
            if (sizeId != null && quantity != null) {
                quantityMap.put(sizeId, quantity);
            }
        }

        return quantityMap;
    }
}

package com.emenu.features.product.utils;

import com.emenu.features.product.repository.ProductFavoriteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Helper class for ProductMapper to check favorite status
 * This avoids circular dependencies with ProductFavoriteService
 */
@Component
@RequiredArgsConstructor
public class ProductFavoriteQueryHelper {

    private final ProductFavoriteRepository favoriteRepository;

    /**
     * 🚀 BATCH QUERY: Get favorite product IDs for a user
     */
    public List<UUID> getFavoriteProductIds(UUID userId, List<UUID> productIds) {
        if (userId == null || productIds == null || productIds.isEmpty()) {
            return List.of();
        }
        return favoriteRepository.findFavoriteProductIdsByUserIdAndProductIds(userId, productIds);
    }

    /**
     * 🚀 SINGLE QUERY: Check if product is favorited by user
     */
    public boolean isFavorited(UUID userId, UUID productId) {
        if (userId == null || productId == null) {
            return false;
        }
        return favoriteRepository.existsByUserIdAndProductIdAndIsDeletedFalse(userId, productId);
    }
}
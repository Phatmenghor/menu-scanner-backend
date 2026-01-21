package com.emenu.features.main.utils;

import com.emenu.features.main.repository.ProductFavoriteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProductFavoriteQueryHelper {

    private final ProductFavoriteRepository favoriteRepository;

    public List<UUID> getFavoriteProductIds(UUID userId, List<UUID> productIds) {
        if (userId == null || productIds == null || productIds.isEmpty()) {
            return List.of();
        }
        return favoriteRepository.findFavoriteProductIdsByUserIdAndProductIds(userId, productIds);
    }

    public boolean isFavorited(UUID userId, UUID productId) {
        if (userId == null || productId == null) {
            return false;
        }
        return favoriteRepository.existsByUserIdAndProductIdAndIsDeletedFalse(userId, productId);
    }
}
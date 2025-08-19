package com.emenu.features.product.mapper;

import com.emenu.features.product.repository.ProductFavoriteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductFavoriteService {

    private final ProductFavoriteRepository favoriteRepository;

    /**
     * 🚀 BATCH QUERY: Get favorite product IDs for a user
     */
    public List<UUID> getFavoriteProductIds(UUID userId, List<UUID> productIds) {
        return favoriteRepository.findFavoriteProductIdsByUserIdAndProductIds(userId, productIds);
    }

    /**
     * 🚀 SINGLE QUERY: Check if product is favorited by user
     */
    public boolean isFavorited(UUID userId, UUID productId) {
        return favoriteRepository.existsByUserIdAndProductIdAndIsDeletedFalse(userId, productId);
    }
}
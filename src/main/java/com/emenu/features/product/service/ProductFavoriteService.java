package com.emenu.features.product.service;

import com.emenu.features.product.dto.filter.ProductFilterDto;
import com.emenu.features.product.dto.response.FavoriteRemoveAllDto;
import com.emenu.features.product.dto.response.FavoriteToggleDto;
import com.emenu.features.product.dto.response.ProductListDto;
import com.emenu.shared.dto.PaginationResponse;

import java.util.List;
import java.util.UUID;

public interface ProductFavoriteService {
    FavoriteToggleDto toggleFavorite(UUID productId);
    PaginationResponse<ProductListDto> getUserFavorites(ProductFilterDto filter);
    FavoriteRemoveAllDto removeAllFavorites();
    List<UUID> getFavoriteProductIds(UUID userId, List<UUID> productIds);
}
package com.emenu.features.main.service;

import com.emenu.features.main.dto.filter.ProductFilterDto;
import com.emenu.features.main.dto.response.FavoriteRemoveAllDto;
import com.emenu.features.main.dto.response.FavoriteToggleDto;
import com.emenu.features.main.dto.response.ProductListDto;
import com.emenu.shared.dto.PaginationResponse;

import java.util.UUID;

public interface ProductFavoriteService {
    FavoriteToggleDto toggleFavorite(UUID productId, UUID businessId);
    PaginationResponse<ProductListDto> getUserFavorites(UUID businessId, ProductFilterDto filter);
    FavoriteRemoveAllDto removeAllFavorites(UUID businessId);
}

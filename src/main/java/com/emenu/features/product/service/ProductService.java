package com.emenu.features.product.service;

import com.emenu.features.product.dto.filter.ProductFilterRequest;
import com.emenu.features.product.dto.request.ProductCreateRequest;
import com.emenu.features.product.dto.response.*;
import com.emenu.features.product.dto.update.ProductUpdateRequest;
import com.emenu.shared.dto.PaginationResponse;

import java.util.UUID;

public interface ProductService {
    
    // CRUD Operations
    ProductResponse createProduct(ProductCreateRequest request);
    PaginationResponse<ProductResponse> getAllProducts(ProductFilterRequest filter);
    ProductResponse getProductById(UUID id);
    ProductResponse updateProduct(UUID id, ProductUpdateRequest request);
    ProductResponse deleteProduct(UUID id);
    
    // Public Operations (for customer-facing features)
    ProductResponse getProductByIdPublic(UUID id);

    // Enhanced Favorite Operations
    FavoriteToggleResponse toggleFavorite(UUID productId);
    FavoriteToggleResponse setFavoriteStatus(UUID productId, boolean favorite);
    void addToFavorites(UUID productId);
    void removeFromFavorites(UUID productId);
    PaginationResponse<ProductResponse> getUserFavorites(ProductFilterRequest filter);
    FavoriteRemoveAllResponse removeAllFavorites();
    FavoriteCountResponse getFavoriteCount();

    // Unified Promotion Management
    ProductPromotionResetResponse resetProductPromotion(UUID productId);
    SizePromotionResetResponse resetSizePromotion(UUID productId, UUID sizeId);
    ExpiredPromotionResetResponse resetExpiredPromotions();
    BusinessPromotionResetResponse resetAllBusinessPromotions();
}
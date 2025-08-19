package com.emenu.features.product.controller;

import com.emenu.features.product.dto.filter.ProductFilterDto;
import com.emenu.features.product.dto.response.FavoriteRemoveAllDto;
import com.emenu.features.product.dto.response.FavoriteToggleDto;
import com.emenu.features.product.dto.response.ProductListDto;
import com.emenu.features.product.service.ProductFavoriteService;
import com.emenu.shared.dto.ApiResponse;
import com.emenu.shared.dto.PaginationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/product-favorites")
@RequiredArgsConstructor
@Slf4j
public class ProductFavoriteController {
    
    private final ProductFavoriteService favoriteService;

    @PostMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse<FavoriteToggleDto>> toggleFavorite(@PathVariable UUID id) {
        log.info("Toggling favorite for product: {}", id);
        
        FavoriteToggleDto result = favoriteService.toggleFavorite(id);
        
        return ResponseEntity.ok(ApiResponse.success(result.getMessage(), result));
    }

    @PostMapping("/{id}/favorite")
    public ResponseEntity<ApiResponse<FavoriteToggleDto>> setFavoriteStatus(
            @PathVariable UUID id,
            @RequestParam boolean favorite) {
        
        log.info("Setting favorite status to {} for product: {}", favorite, id);
        
        // For now, we use toggle - in future could implement specific set operation
        FavoriteToggleDto result = favoriteService.toggleFavorite(id);
        
        String action = favorite ? "added to" : "removed from";
        return ResponseEntity.ok(ApiResponse.success("Product " + action + " favorites successfully", result));
    }

    @PostMapping("/favorites")
    public ResponseEntity<ApiResponse<PaginationResponse<ProductListDto>>> getUserFavorites(
            @Valid @RequestBody ProductFilterDto filter) {
        
        log.info("Getting user's favorite products");
        
        PaginationResponse<ProductListDto> favorites = favoriteService.getUserFavorites(filter);
        
        return ResponseEntity.ok(ApiResponse.success("Favorite products retrieved successfully", favorites));
    }

    @DeleteMapping("/favorites/all")
    public ResponseEntity<ApiResponse<FavoriteRemoveAllDto>> removeAllFavorites() {
        log.info("Removing all favorites for current user");
        
        FavoriteRemoveAllDto result = favoriteService.removeAllFavorites();
        
        return ResponseEntity.ok(ApiResponse.success("All favorites removed successfully", result));
    }
}
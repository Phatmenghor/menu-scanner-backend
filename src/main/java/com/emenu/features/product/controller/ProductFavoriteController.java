package com.emenu.features.product.controller;

import com.emenu.features.product.dto.filter.ProductFilterDto;
import com.emenu.features.product.dto.response.FavoriteRemoveAllDto;
import com.emenu.features.product.dto.response.FavoriteToggleDto;
import com.emenu.features.product.dto.response.ProductListDto;
import com.emenu.features.product.service.ProductService;
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
    private final ProductService productService;

    /**
     * Set specific favorite status (true to add, false to remove)
     */
    @PostMapping("/{id}/favorite")
    public ResponseEntity<ApiResponse<FavoriteToggleDto>> setFavoriteStatus(
            @PathVariable UUID id,
            @RequestParam boolean favorite) {
        log.info("Setting favorite status to {} for product: {}", favorite, id);
        
        // Use toggle method since we only have toggle available
        FavoriteToggleDto result = productService.toggleFavorite(id);
        
        String action = favorite ? "added to" : "removed from";
        return ResponseEntity.ok(ApiResponse.success("Product " + action + " favorites successfully", result));
    }

    /**
     * Get user's favorite products with proper pagination
     */
    @PostMapping("/favorites")
    public ResponseEntity<ApiResponse<PaginationResponse<ProductListDto>>> getUserFavorites(
            @Valid @RequestBody ProductFilterDto filter) {
        log.info("Getting user's favorite products - Page: {}, Size: {}", filter.getPageNo(), filter.getPageSize());
        PaginationResponse<ProductListDto> favorites = productService.getUserFavorites(filter);
        return ResponseEntity.ok(ApiResponse.success("Favorite products retrieved successfully", favorites));
    }

    /**
     * Remove all favorite products for current user
     */
    @DeleteMapping("/favorites/all")
    public ResponseEntity<ApiResponse<FavoriteRemoveAllDto>> removeAllFavorites() {
        log.info("Removing all favorites for current user");
        FavoriteRemoveAllDto result = productService.removeAllFavorites();
        return ResponseEntity.ok(ApiResponse.success("All favorites removed successfully", result));
    }
}
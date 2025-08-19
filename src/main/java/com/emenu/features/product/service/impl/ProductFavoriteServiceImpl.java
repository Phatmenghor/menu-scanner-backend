package com.emenu.features.product.service.impl;

import com.emenu.exception.custom.NotFoundException;
import com.emenu.exception.custom.ValidationException;
import com.emenu.features.auth.models.User;
import com.emenu.features.product.dto.filter.ProductFilterDto;
import com.emenu.features.product.dto.response.FavoriteRemoveAllDto;
import com.emenu.features.product.dto.response.FavoriteToggleDto;
import com.emenu.features.product.dto.response.ProductListDto;
import com.emenu.features.product.mapper.FavoriteMapper;
import com.emenu.features.product.mapper.ProductMapper;
import com.emenu.features.product.models.Product;
import com.emenu.features.product.models.ProductFavorite;
import com.emenu.features.product.repository.ProductFavoriteRepository;
import com.emenu.features.product.repository.ProductRepository;
import com.emenu.features.product.service.ProductFavoriteService;
import com.emenu.security.SecurityUtils;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.mapper.PaginationMapper;
import com.emenu.shared.pagination.PaginationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductFavoriteServiceImpl implements ProductFavoriteService {

    private final ProductRepository productRepository;
    private final ProductFavoriteRepository favoriteRepository;
    private final ProductMapper productMapper;
    private final FavoriteMapper favoriteMapper;
    private final PaginationMapper paginationMapper;
    private final SecurityUtils securityUtils;

    @Override
    public FavoriteToggleDto toggleFavorite(UUID productId) {
        User currentUser = securityUtils.getCurrentUser();
        UUID userId = currentUser.getId();
        
        log.info("Toggling favorite for product: {} by user: {}", productId, userId);

        // Verify product exists and is active
        Product product = productRepository.findByIdAndIsDeletedFalse(productId)
                .orElseThrow(() -> new NotFoundException("Product not found with ID: " + productId));
        
        if (!product.isActive()) {
            throw new ValidationException("Cannot favorite inactive product");
        }

        boolean isFavorited = favoriteRepository.existsByUserIdAndProductIdAndIsDeletedFalse(userId, productId);

        String action;
        boolean finalStatus;

        if (!isFavorited) {
            // Add to favorites
            ProductFavorite favorite = new ProductFavorite(userId, productId);
            favoriteRepository.save(favorite);
            productRepository.incrementFavoriteCount(productId);
            action = "added";
            finalStatus = true;
        } else {
            // Remove from favorites
            favoriteRepository.deleteByUserIdAndProductId(userId, productId);
            productRepository.decrementFavoriteCount(productId);
            action = "removed";
            finalStatus = false;
        }

        FavoriteToggleDto result = favoriteMapper.createToggleResponse(productId, userId, finalStatus, action);
        log.info("Favorite toggle completed: {} product {} for user {}", action, productId, userId);
        
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<ProductListDto> getUserFavorites(ProductFilterDto filter) {
        UUID userId = securityUtils.getCurrentUserId();
        log.info("Getting favorites for user: {}", userId);

        Pageable pageable = PaginationUtils.createPageable(
            filter.getPageNo() != null ? filter.getPageNo() - 1 : null,
            filter.getPageSize(), 
            filter.getSortBy(), 
            filter.getSortDirection()
        );
        
        // Use optimized favorites query
        Page<Product> favoritePage = productRepository.findUserFavorites(userId, pageable);
        
        // Map to DTOs and set all as favorited
        PaginationResponse<ProductListDto> response = paginationMapper.toPaginationResponse(
            favoritePage,
                productMapper::toListDtos
        );
        
        // All products in favorites are favorited by definition
        response.getContent().forEach(product -> product.setIsFavorited(true));

        log.info("Retrieved {} favorites for user: {}", response.getContent().size(), userId);
        return response;
    }

    @Override
    public FavoriteRemoveAllDto removeAllFavorites() {
        UUID userId = securityUtils.getCurrentUserId();
        log.info("Removing all favorites for user: {}", userId);
        
        // Remove all favorites for the user
        int removedCount = favoriteRepository.deleteAllByUserId(userId);
        
        log.info("Removed {} favorites for user: {}", removedCount, userId);
        
        return FavoriteRemoveAllDto.builder()
                .userId(userId)
                .removedCount(removedCount)
                .timestamp(LocalDateTime.now())
                .message(String.format("Removed %d products from favorites", removedCount))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UUID> getFavoriteProductIds(UUID userId, List<UUID> productIds) {
        if (userId == null || productIds == null || productIds.isEmpty()) {
            return List.of();
        }
        return favoriteRepository.findFavoriteProductIdsByUserIdAndProductIds(userId, productIds);
    }
}
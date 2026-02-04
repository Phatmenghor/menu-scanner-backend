package com.emenu.features.main.service.impl;

import com.emenu.exception.custom.NotFoundException;
import com.emenu.exception.custom.ValidationException;
import com.emenu.features.auth.models.User;
import com.emenu.features.main.dto.filter.ProductFilterDto;
import com.emenu.features.main.dto.response.FavoriteRemoveAllDto;
import com.emenu.features.main.dto.response.FavoriteToggleDto;
import com.emenu.features.main.dto.response.ProductListDto;
import com.emenu.features.main.mapper.FavoriteMapper;
import com.emenu.features.main.mapper.ProductMapper;
import com.emenu.features.main.models.Product;
import com.emenu.features.main.models.ProductFavorite;
import com.emenu.features.main.repository.ProductFavoriteRepository;
import com.emenu.features.main.repository.ProductRepository;
import com.emenu.features.main.service.ProductFavoriteService;
import com.emenu.features.order.utils.CartQueryHelper;
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
import java.util.*;

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
    private final CartQueryHelper cartQueryHelper;

    @Override
    public FavoriteToggleDto toggleFavorite(UUID productId) {
        User currentUser = securityUtils.getCurrentUser();
        UUID userId = currentUser.getId();

        log.info("Toggling favorite - Product: {}, User: {}", productId, userId);

        Product product = productRepository.findByIdAndIsDeletedFalse(productId)
                .orElseThrow(() -> new NotFoundException("Product not found: " + productId));

        if (!product.isActive()) {
            throw new ValidationException("Cannot favorite inactive product");
        }

        boolean isFavorited = favoriteRepository.existsByUserIdAndProductIdAndIsDeletedFalse(userId, productId);

        String action;
        boolean finalStatus;

        if (!isFavorited) {
            ProductFavorite favorite = new ProductFavorite(userId, productId);
            favoriteRepository.save(favorite);
            productRepository.incrementFavoriteCount(productId);
            action = "added";
            finalStatus = true;
            log.info("Favorite added - Product: {}, User: {}", productId, userId);
        } else {
            favoriteRepository.deleteByUserIdAndProductId(userId, productId);
            productRepository.decrementFavoriteCount(productId);
            action = "removed";
            finalStatus = false;
            log.info("Favorite removed - Product: {}, User: {}", productId, userId);
        }

        return favoriteMapper.createToggleResponse(productId, userId, finalStatus, action);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<ProductListDto> getUserFavorites(ProductFilterDto filter) {
        UUID userId = securityUtils.getCurrentUserId();
        UUID businessId = filter.getBusinessId();
        log.info("Getting favorites - User: {}, Business: {}", userId, businessId);

        Pageable pageable = PaginationUtils.createPageable(
            filter.getPageNo(),
            filter.getPageSize(),
            "createdAt",
            "DESC"
        );

        Page<Product> favoritePage;
        if (businessId != null) {
            favoritePage = productRepository.findUserFavoritesByBusiness(userId, businessId, pageable);
        } else {
            favoritePage = productRepository.findUserFavorites(userId, pageable);
        }

        // Recalculate display fields from current sizes
        favoritePage.getContent().forEach(Product::syncDisplayFieldsFromSizes);

        PaginationResponse<ProductListDto> response = productMapper.toPaginationResponse(
            favoritePage,
            paginationMapper
        );

        if (!response.getContent().isEmpty()) {
            List<UUID> productIds = response.getContent().stream()
                    .map(ProductListDto::getId)
                    .toList();

            Map<UUID, Integer> cartQuantities = cartQueryHelper.getProductQuantitiesInCart(
                    userId, businessId, productIds
            );

            response.getContent().forEach(product -> {
                product.setIsFavorited(true);
                product.setQuantityInCart(cartQuantities.getOrDefault(product.getId(), 0));
            });
        }

        log.info("Retrieved {} favorites - User: {}", response.getContent().size(), userId);
        return response;
    }

    @Override
    public FavoriteRemoveAllDto removeAllFavorites(UUID businessId) {
        UUID userId = securityUtils.getCurrentUserId();
        log.info("Removing all favorites - User: {}, Business: {}", userId, businessId);

        int removedCount = favoriteRepository.deleteAllByUserIdAndBusinessId(userId, businessId);

        log.info("Removed {} favorites - User: {}, Business: {}", removedCount, userId, businessId);

        return FavoriteRemoveAllDto.builder()
                .userId(userId)
                .removedCount(removedCount)
                .timestamp(LocalDateTime.now())
                .message(String.format("Removed %d products from favorites", removedCount))
                .build();
    }
}

package com.emenu.features.product.service.impl;

import com.emenu.exception.custom.NotFoundException;
import com.emenu.exception.custom.ValidationException;
import com.emenu.features.auth.models.User;
import com.emenu.features.product.dto.filter.ProductFilterRequest;
import com.emenu.features.product.dto.request.ProductCreateRequest;
import com.emenu.features.product.dto.response.*;
import com.emenu.features.product.dto.update.ProductUpdateRequest;
import com.emenu.features.product.mapper.*;
import com.emenu.features.product.models.Product;
import com.emenu.features.product.models.ProductFavorite;
import com.emenu.features.product.models.ProductImage;
import com.emenu.features.product.models.ProductSize;
import com.emenu.features.product.repository.ProductFavoriteRepository;
import com.emenu.features.product.repository.ProductImageRepository;
import com.emenu.features.product.repository.ProductRepository;
import com.emenu.features.product.repository.ProductSizeRepository;
import com.emenu.features.product.service.ProductService;
import com.emenu.security.SecurityUtils;
import com.emenu.shared.dto.PaginationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductSizeRepository productSizeRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductFavoriteRepository productFavoriteRepository;
    
    // Original mappers for CRUD operations
    private final ProductMapper productMapper;
    private final ProductSizeMapper productSizeMapper;
    private final ProductImageMapper productImageMapper;
    private final FavoriteResponseMapper favoriteMapper;
    private final PromotionResponseMapper promotionMapper;
    
    // Fast listing mapper
    private final ProductListingMapper productListingMapper;
    
    private final SecurityUtils securityUtils;

    // ================================
    // FAST LISTING OPERATIONS - ACTUALLY USING NATIVE QUERIES
    // ================================

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<ProductResponse> getAllProducts(ProductFilterRequest filter) {
        long startTime = System.currentTimeMillis();
        
        log.info("Getting products with FAST NATIVE QUERY - Page: {}, Size: {}", 
                filter.getPageNo(), filter.getPageSize());

        try {
            Optional<User> currentUser = securityUtils.getCurrentUserOptional();
            
            // Apply business filter for authenticated business users
            if (currentUser.isPresent() && currentUser.get().isBusinessUser() && filter.getBusinessId() == null) {
                filter.setBusinessId(currentUser.get().getBusinessId());
            }

            // Calculate pagination
            int pageNo = filter.getPageNo() != null && filter.getPageNo() > 0 ? filter.getPageNo() - 1 : 0;
            int pageSize = filter.getPageSize() != null ? filter.getPageSize() : 10;
            int offset = pageNo * pageSize;

            // Convert enum to string for native query
            String statusStr = filter.getStatus() != null ? filter.getStatus().name() : null;

            // EXECUTE FAST NATIVE QUERIES
            List<Object[]> rows = productRepository.findProductsForListing(
                    filter.getBusinessId(),
                    filter.getCategoryId(),
                    filter.getBrandId(),
                    statusStr,
                    filter.getSearch(),
                    pageSize,
                    offset
            );

            long totalElements = productRepository.countProductsForListing(
                    filter.getBusinessId(),
                    filter.getCategoryId(),
                    filter.getBrandId(),
                    statusStr,
                    filter.getSearch()
            );

            // Convert native query results to lightweight DTOs
            PaginationResponse<ProductListingResponse> fastResult = productListingMapper.createFastPaginationResponse(
                    rows, 
                    totalElements, 
                    pageNo, 
                    pageSize,
                    currentUser.map(User::getId).orElse(null)
            );
            
            // Convert ProductListingResponse to ProductResponse for API compatibility
            List<ProductResponse> productResponses = fastResult.getContent().stream()
                    .map(this::convertListingToProductResponse)
                    .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

            long duration = System.currentTimeMillis() - startTime;
            log.info("FAST NATIVE QUERY completed in {}ms - {} products returned", 
                    duration, productResponses.size());
            
            // Return with ProductResponse structure for API compatibility
            return PaginationResponse.<ProductResponse>builder()
                    .content(productResponses)
                    .pageNo(fastResult.getPageNo())
                    .pageSize(fastResult.getPageSize())
                    .totalElements(fastResult.getTotalElements())
                    .totalPages(fastResult.getTotalPages())
                    .first(fastResult.isFirst())
                    .last(fastResult.isLast())
                    .hasNext(fastResult.isHasNext())
                    .hasPrevious(fastResult.isHasPrevious())
                    .build();
            
        } catch (Exception e) {
            log.error("Error in FAST NATIVE QUERY: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve products with fast query", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<ProductResponse> getUserFavorites(ProductFilterRequest filter) {
        long startTime = System.currentTimeMillis();
        
        UUID userId = securityUtils.getCurrentUserId();
        log.info("Getting user favorites with FAST NATIVE QUERY - User: {}, Page: {}, Size: {}", 
                userId, filter.getPageNo(), filter.getPageSize());

        try {
            // Calculate pagination
            int pageNo = filter.getPageNo() != null && filter.getPageNo() > 0 ? filter.getPageNo() - 1 : 0;
            int pageSize = filter.getPageSize() != null ? filter.getPageSize() : 10;
            int offset = pageNo * pageSize;

            // EXECUTE FAST NATIVE QUERIES FOR FAVORITES
            List<Object[]> rows = productRepository.findUserFavoriteProducts(userId, pageSize, offset);
            long totalElements = productRepository.countUserFavorites(userId);

            // Convert native query results to lightweight DTOs
            PaginationResponse<ProductListingResponse> fastResult = productListingMapper.createFastPaginationResponse(
                    rows, 
                    totalElements, 
                    pageNo, 
                    pageSize,
                    userId // All are favorites
            );
            
            // Convert to ProductResponse for API compatibility
            List<ProductResponse> productResponses = fastResult.getContent().stream()
                    .map(listing -> {
                        ProductResponse response = convertListingToProductResponse(listing);
                        response.setIsFavorited(true); // All are favorites
                        return response;
                    })
                    .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

            long duration = System.currentTimeMillis() - startTime;
            log.info("FAST FAVORITES QUERY completed in {}ms - {} products returned", 
                    duration, productResponses.size());
            
            return PaginationResponse.<ProductResponse>builder()
                    .content(productResponses)
                    .pageNo(fastResult.getPageNo())
                    .pageSize(fastResult.getPageSize())
                    .totalElements(fastResult.getTotalElements())
                    .totalPages(fastResult.getTotalPages())
                    .first(fastResult.isFirst())
                    .last(fastResult.isLast())
                    .hasNext(fastResult.isHasNext())
                    .hasPrevious(fastResult.isHasPrevious())
                    .build();
            
        } catch (Exception e) {
            log.error("Error in FAST FAVORITES QUERY: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve favorites with fast query", e);
        }
    }

    /**
     * Convert ProductListingResponse to ProductResponse for API compatibility
     * This maintains the same API structure while using fast queries internally
     */
    private ProductResponse convertListingToProductResponse(ProductListingResponse listing) {
        ProductResponse response = new ProductResponse();
        
        // Copy all fields from listing to response
        response.setId(listing.getId());
        response.setBusinessId(listing.getBusinessId());
        response.setBusinessName(listing.getBusinessName());
        response.setCategoryId(listing.getCategoryId());
        response.setCategoryName(listing.getCategoryName());
        response.setBrandId(listing.getBrandId());
        response.setBrandName(listing.getBrandName());
        response.setName(listing.getName());
        response.setDescription(listing.getDescription());
        response.setStatus(listing.getStatus());
        response.setPrice(listing.getPrice());
        response.setPromotionType(listing.getPromotionType());
        response.setPromotionValue(listing.getPromotionValue());
        response.setDisplayPrice(listing.getDisplayPrice());
        response.setHasPromotionActive(listing.getHasActivePromotion());
        response.setHasSizes(listing.getHasSizes());
        response.setMainImageUrl(listing.getMainImageUrl());
        response.setFavoriteCount(listing.getFavoriteCount());
        response.setViewCount(listing.getViewCount());
        response.setIsFavorited(listing.getIsFavorited());
        response.setPublicUrl(listing.getPublicUrl());
        response.setCreatedAt(listing.getCreatedAt());
        response.setUpdatedAt(listing.getUpdatedAt());
        
        // Set empty collections for listings (as requested)
        response.setImages(List.of());
        response.setSizes(List.of());
        
        // Set fields that are not needed for listings
        response.setPromotionFromDate(null);
        response.setPromotionToDate(null);
        
        return response;
    }

    // ================================
    // SINGLE PRODUCT OPERATIONS - FULL DETAILS
    // ================================

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(UUID id) {
        long startTime = System.currentTimeMillis();
        
        log.info("Getting FULL product details by ID: {}", id);

        try {
            Product product = findProductByIdWithDetails(id);
            ProductResponse response = productMapper.toResponse(product);
            
            Optional<User> currentUser = securityUtils.getCurrentUserOptional();
            if (currentUser.isPresent()) {
                response = productMapper.enrichWithFavoriteStatus(response, currentUser.get().getId());
            } else {
                response.setIsFavorited(false);
            }
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("FULL product details retrieved in {}ms", duration);
            
            return response;
            
        } catch (Exception e) {
            log.error("Error getting FULL product by ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductByIdPublic(UUID id) {
        long startTime = System.currentTimeMillis();
        
        log.info("Getting FULL product details (public) by ID: {}", id);

        try {
            Product product = findProductByIdWithDetails(id);
            productRepository.incrementViewCount(id);
            
            ProductResponse response = productMapper.toResponse(product);
            
            Optional<User> currentUser = securityUtils.getCurrentUserOptional();
            if (currentUser.isPresent()) {
                response = productMapper.enrichWithFavoriteStatus(response, currentUser.get().getId());
            } else {
                response.setIsFavorited(false);
            }
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("FULL public product details retrieved in {}ms", duration);
            
            return response;
            
        } catch (Exception e) {
            log.error("Error getting FULL public product by ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    // ================================
    // CRUD OPERATIONS (UNCHANGED)
    // ================================

    @Override
    public ProductResponse createProduct(ProductCreateRequest request) {
        log.info("Creating product: {}", request.getName());

        User currentUser = securityUtils.getCurrentUser();
        validateUserBusinessAssociation(currentUser);

        Product product = productMapper.toEntity(request);
        product.setBusinessId(currentUser.getBusinessId());
        Product savedProduct = productRepository.save(product);

        createProductSizes(savedProduct, request.getSizes());
        createProductImages(savedProduct, request.getImages());

        log.info("Product created successfully: {} for business: {}", 
                savedProduct.getName(), currentUser.getBusinessId());
        
        return getProductById(savedProduct.getId());
    }

    @Override
    public ProductResponse updateProduct(UUID id, ProductUpdateRequest request) {
        log.info("Updating product: {}", id);

        Product product = productRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        User currentUser = securityUtils.getCurrentUser();
        validateBusinessOwnership(product, currentUser);

        product = loadProductCollections(product);

        try {
            updateProductSizes(product, request.getSizes());
            updateProductImages(product, request.getImages());
            productMapper.updateEntity(request, product);
            
            Product updatedProduct = productRepository.saveAndFlush(product);
            log.info("Product updated successfully: {}", id);
            return getProductById(updatedProduct.getId());
            
        } catch (Exception e) {
            log.error("Error updating product {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to update product: " + e.getMessage(), e);
        }
    }

    @Override
    public ProductResponse deleteProduct(UUID id) {
        Product product = findProductByIdWithDetails(id);
        User currentUser = securityUtils.getCurrentUser();
        validateBusinessOwnership(product, currentUser);
        
        product.softDelete();
        product = productRepository.save(product);

        log.info("Product deleted successfully: {}", id);
        return productMapper.toResponse(product);
    }

    // ================================
    // FAVORITES MANAGEMENT (UNCHANGED)
    // ================================

    @Override
    public FavoriteToggleResponse setFavoriteStatus(UUID productId, boolean favorite) {
        User currentUser = securityUtils.getCurrentUser();
        boolean isFavorited = productFavoriteRepository.existsByUserIdAndProductId(
                currentUser.getId(), productId);

        String action;
        boolean finalStatus;

        if (favorite && !isFavorited) {
            ProductFavorite favoriteEntity = new ProductFavorite(currentUser.getId(), productId);
            productFavoriteRepository.save(favoriteEntity);
            productRepository.incrementFavoriteCount(productId);
            action = "added";
            finalStatus = true;
            log.info("Added product {} to favorites for user: {}", productId, currentUser.getId());
        } else if (!favorite && isFavorited) {
            productFavoriteRepository.deleteByUserIdAndProductId(currentUser.getId(), productId);
            productRepository.decrementFavoriteCount(productId);
            action = "removed";
            finalStatus = false;
            log.info("Removed product {} from favorites for user: {}", productId, currentUser.getId());
        } else {
            action = "unchanged";
            finalStatus = isFavorited;
        }

        return favoriteMapper.createToggleResponse(productId, currentUser.getId(), finalStatus, action);
    }

    @Override
    public FavoriteRemoveAllResponse removeAllFavorites() {
        UUID userId = securityUtils.getCurrentUserId();
        
        List<ProductFavorite> favorites = productFavoriteRepository.findFavoritesByUserId(userId);
        List<UUID> productIds = favorites.stream()
                .map(ProductFavorite::getProductId)
                .toList();

        int removedCount = productFavoriteRepository.deleteAllByUserId(userId);
        productIds.forEach(productRepository::decrementFavoriteCount);

        log.info("Removed all {} favorites for user: {}", removedCount, userId);
        return favoriteMapper.createRemoveAllResponse(userId, removedCount);
    }

    // ================================
    // PROMOTION MANAGEMENT (UNCHANGED)
    // ================================

    @Override
    public ProductPromotionResetResponse resetProductPromotion(UUID productId) {
        Product product = findProductByIdWithDetails(productId);
        User currentUser = securityUtils.getCurrentUser();
        
        validateUserBusinessAssociation(currentUser);
        validateBusinessOwnership(product, currentUser);
        
        boolean productHadPromotion = product.isPromotionActive();
        
        List<ProductSize> sizes = new ArrayList<>(product.getSizes());
        long sizesWithPromotions = productSizeMapper.countActivePromotions(sizes);
        productSizeMapper.removeAllPromotions(sizes);
        
        product.removePromotion();
        productRepository.save(product);
        
        log.info("Reset all promotions for product {} - Product: {}, Sizes: {}", 
                productId, productHadPromotion, sizesWithPromotions);
        
        return promotionMapper.createProductResetResponse(
                product, currentUser.getBusinessId(), productHadPromotion, (int) sizesWithPromotions);
    }

    @Override
    public SizePromotionResetResponse resetSizePromotion(UUID productId, UUID sizeId) {
        Product product = findProductByIdWithDetails(productId);
        User currentUser = securityUtils.getCurrentUser();
        
        validateUserBusinessAssociation(currentUser);
        validateBusinessOwnership(product, currentUser);
        
        ProductSize productSize = productSizeRepository.findByIdAndIsDeletedFalse(sizeId)
                .orElseThrow(() -> new NotFoundException("Size not found"));
        
        boolean hadPromotion = productSizeMapper.isPromotionActive(productSize);
        String originalPromotionType = productSize.getPromotionType() != null ? 
                productSize.getPromotionType().name() : null;
        
        productSizeMapper.removeAllPromotions(List.of(productSize));
        productSizeRepository.save(productSize);
        
        log.info("Reset promotion for size {} ({}) of product {} for business {}", 
                sizeId, productSize.getName(), product.getName(), currentUser.getBusinessId());
        
        return promotionMapper.createSizeResetResponse(
                product, productSize, currentUser.getBusinessId(), hadPromotion, originalPromotionType);
    }

    @Override
    public BusinessPromotionResetResponse resetAllBusinessPromotions() {
        User currentUser = securityUtils.getCurrentUser();
        validateUserBusinessAssociation(currentUser);

        int productPromotionsReset = productRepository.clearAllPromotionsForBusiness(currentUser.getBusinessId());
        int sizePromotionsReset = productSizeRepository.clearAllPromotionsForBusiness(currentUser.getBusinessId());
        
        log.info("Reset all {} promotions for business {} ({} products, {} sizes)", 
                productPromotionsReset + sizePromotionsReset, currentUser.getBusinessId(), 
                productPromotionsReset, sizePromotionsReset);
        
        return promotionMapper.createBusinessResetResponse(
                currentUser.getBusinessId(), productPromotionsReset, sizePromotionsReset);
    }

    // ================================
    // HELPER METHODS (UNCHANGED)
    // ================================

    private Product findProductByIdWithDetails(UUID id) {
        Product product = productRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));
        return loadProductCollections(product);
    }

    private Product loadProductCollections(Product product) {
        if (product.getImages() == null) {
            product.setImages(new ArrayList<>());
        }
        if (product.getSizes() == null) {
            product.setSizes(new ArrayList<>());
        }

        List<ProductImage> images = loadProductImages(product.getId());
        images.forEach(image -> image.setProduct(product));
        product.setImages(productImageMapper.getSortedImages(images));
        
        List<ProductSize> sizes = loadProductSizes(product.getId());
        sizes.forEach(size -> size.setProduct(product));
        product.setSizes(productSizeMapper.getSortedSizes(sizes));
        
        return product;
    }

    private List<ProductImage> loadProductImages(UUID productId) {
        return productImageRepository.findByProductIdOrderByMainAndSort(productId);
    }

    private List<ProductSize> loadProductSizes(UUID productId) {
        return productRepository.findByIdWithSizes(productId)
                .map(Product::getSizes)
                .orElse(new ArrayList<>());
    }

    private void createProductSizes(Product product, List<com.emenu.features.product.dto.request.ProductSizeRequest> sizeRequests) {
        if (sizeRequests != null && !sizeRequests.isEmpty()) {
            ProductSizeMapper.SizeCreationResult sizeResult = 
                    productSizeMapper.processSizeCreation(sizeRequests, product.getId());
            
            sizeResult.sizes.forEach(size -> size.setProduct(product));
            productSizeRepository.saveAll(sizeResult.sizes);
        }
    }

    private void createProductImages(Product product, List<com.emenu.features.product.dto.request.ProductImageRequest> imageRequests) {
        if (imageRequests != null && !imageRequests.isEmpty()) {
            ProductImageMapper.ImageCreationResult imageResult = 
                    productImageMapper.processImageCreation(imageRequests, product.getId());
            
            imageResult.images.forEach(image -> image.setProduct(product));
            productImageRepository.saveAll(imageResult.images);
        }
    }

    private void updateProductSizes(Product product, List<com.emenu.features.product.dto.request.ProductSizeRequest> sizeRequests) {
        if (product.getSizes() == null) {
            product.setSizes(new ArrayList<>());
        }

        if (sizeRequests == null || sizeRequests.isEmpty()) {
            product.setSizes(new ArrayList<>());
            return;
        }

        List<ProductSize> existingSizes = new ArrayList<>(product.getSizes());
        ProductSizeMapper.SizeUpdateResult sizeResult =
                productSizeMapper.processSizeUpdate(sizeRequests, existingSizes, product.getId());

        product.setSizes(sizeResult.sizes);
    }

    private void updateProductImages(Product product, List<com.emenu.features.product.dto.request.ProductImageRequest> imageRequests) {
        if (product.getImages() == null) {
            product.setImages(new ArrayList<>());
        }

        if (imageRequests == null || imageRequests.isEmpty()) {
            product.setImages(new ArrayList<>());
            return;
        }

        List<ProductImage> existingImages = new ArrayList<>(product.getImages());
        ProductImageMapper.ImageUpdateResult imageResult = 
                productImageMapper.processImageUpdate(imageRequests, existingImages, product.getId());
        
        product.setImages(imageResult.images);
    }

    private void validateUserBusinessAssociation(User user) {
        if (user.getBusinessId() == null) {
            throw new ValidationException("User is not associated with any business");
        }
    }

    private void validateBusinessOwnership(Product product, User user) {
        if (!product.getBusinessId().equals(user.getBusinessId())) {
            throw new ValidationException("You can only modify products from your own business");
        }
    }
}
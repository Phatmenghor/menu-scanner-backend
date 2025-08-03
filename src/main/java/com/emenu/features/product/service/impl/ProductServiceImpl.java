package com.emenu.features.product.service.impl;

import com.emenu.enums.product.PromotionType;
import com.emenu.exception.custom.NotFoundException;
import com.emenu.exception.custom.ValidationException;
import com.emenu.features.auth.models.User;
import com.emenu.features.product.dto.filter.ProductFilterRequest;
import com.emenu.features.product.dto.request.ProductCreateRequest;
import com.emenu.features.product.dto.request.ProductImageRequest;
import com.emenu.features.product.dto.request.ProductSizeRequest;
import com.emenu.features.product.dto.response.ProductResponse;
import com.emenu.features.product.dto.update.ProductUpdateRequest;
import com.emenu.features.product.mapper.ProductImageMapper;
import com.emenu.features.product.mapper.ProductMapper;
import com.emenu.features.product.mapper.ProductSizeMapper;
import com.emenu.features.product.models.Product;
import com.emenu.features.product.models.ProductFavorite;
import com.emenu.features.product.models.ProductImage;
import com.emenu.features.product.models.ProductSize;
import com.emenu.features.product.repository.ProductFavoriteRepository;
import com.emenu.features.product.repository.ProductImageRepository;
import com.emenu.features.product.repository.ProductRepository;
import com.emenu.features.product.repository.ProductSizeRepository;
import com.emenu.features.product.service.ProductService;
import com.emenu.features.product.specification.ProductSpecification;
import com.emenu.security.SecurityUtils;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.pagination.PaginationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductSizeRepository productSizeRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductFavoriteRepository productFavoriteRepository;
    private final ProductMapper productMapper;
    private final ProductSizeMapper productSizeMapper;
    private final ProductImageMapper productImageMapper;
    private final SecurityUtils securityUtils;

    @Override
    public ProductResponse createProduct(ProductCreateRequest request) {
        log.info("Creating product: {}", request.getName());

        User currentUser = securityUtils.getCurrentUser();
        if (currentUser.getBusinessId() == null) {
            throw new ValidationException("User is not associated with any business");
        }

        // Create product entity
        Product product = productMapper.toEntity(request);
        product.setBusinessId(currentUser.getBusinessId());

        Product savedProduct = productRepository.save(product);
        // Create sizes
        createProductSizes(savedProduct.getId(), request.getSizes());

        // Create images
        createProductImages(savedProduct.getId(), request.getImages());

        log.info("Product created successfully: {} for business: {}",
                savedProduct.getName(), currentUser.getBusinessId());

        return getProductById(savedProduct.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<ProductResponse> getAllProducts(ProductFilterRequest filter) {

        // Security: Business users can only see products from their business
        User currentUser = securityUtils.getCurrentUser();
        if (currentUser.isBusinessUser() && filter.getBusinessId() == null) {
            filter.setBusinessId(currentUser.getBusinessId());
        }

        Specification<Product> spec = ProductSpecification.buildSpecification(filter);

        int pageNo = filter.getPageNo() != null && filter.getPageNo() > 0 ? filter.getPageNo() - 1 : 0;
        Pageable pageable = PaginationUtils.createPageable(
                pageNo, filter.getPageSize(), filter.getSortBy(), filter.getSortDirection()
        );

        Page<Product> productPage = productRepository.findAll(spec, pageable);
        PaginationResponse<ProductResponse> response = productMapper.toPaginationResponse(productPage);

        // Set favorite status for current user
        setFavoriteStatusForUser(response.getContent());

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(UUID id) {

        Product product = findProductByIdWithDetails(id);

        ProductResponse response = productMapper.toResponse(product);

        // Set favorite status for current user
        setFavoriteStatusForUser(List.of(response));

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductByIdPublic(UUID id) {

        Product product = findProductByIdWithDetails(id);
        productRepository.incrementViewCount(id);
        ProductResponse response = productMapper.toResponse(product);

        setFavoriteStatusForUser(List.of(response));

        return response;
    }

    @Override
    public ProductResponse updateProduct(UUID id, ProductUpdateRequest request) {
        Product product = findProductByIdWithDetails(id);

        // Update basic fields
        productMapper.updateEntity(request, product);

        // Update sizes if provided
        if (request.getSizes() != null) {
            // Delete existing sizes
            productSizeRepository.deleteByProductIdAndIsDeletedFalse(id);
            // Create new sizes
            createProductSizes(id, request.getSizes());
        }

        // Update images if provided
        if (request.getImages() != null) {
            // Delete existing images
            productImageRepository.deleteByProductIdAndIsDeletedFalse(id);
            // Create new images
            createProductImages(id, request.getImages());
        }

        Product updatedProduct = productRepository.save(product);

        log.info("Product updated successfully: {}", id);
        return getProductById(updatedProduct.getId());
    }

    @Override
    public ProductResponse deleteProduct(UUID id) {
        Product product = findProductByIdWithDetails(id);

        product.softDelete();
        product = productRepository.save(product);

        log.info("Product deleted successfully: {}", id);
        return productMapper.toResponse(product);
    }

    // ================================
    // ENHANCED FAVORITE OPERATIONS
    // ================================

    @Override
    public Map<String, Object> toggleFavorite(UUID productId) {
        User currentUser = securityUtils.getCurrentUser();
        boolean isFavorited = productFavoriteRepository.existsByUserIdAndProductId(currentUser.getId(), productId);

        Map<String, Object> result = new HashMap<>();
        
        if (isFavorited) {
            // Remove from favorites
            productFavoriteRepository.deleteByUserIdAndProductId(currentUser.getId(), productId);
            productRepository.decrementFavoriteCount(productId);
            result.put("action", "removed");
            result.put("isFavorited", false);
            log.info("Removed product {} from favorites for user: {}", productId, currentUser.getId());
        } else {
            // Add to favorites
            ProductFavorite favorite = new ProductFavorite(currentUser.getId(), productId);
            productFavoriteRepository.save(favorite);
            productRepository.incrementFavoriteCount(productId);
            result.put("action", "added");
            result.put("isFavorited", true);
            log.info("Added product {} to favorites for user: {}", productId, currentUser.getId());
        }

        result.put("productId", productId);
        result.put("userId", currentUser.getId());
        result.put("timestamp", LocalDateTime.now());
        
        return result;
    }

    @Override
    public Map<String, Object> resetSizePromotion(UUID sizeId) {
        // Find the size (only non-deleted)
        ProductSize productSize = productSizeRepository.findByIdAndIsDeletedFalse(sizeId)
                .orElseThrow(() -> new NotFoundException("Product size not found with ID: " + sizeId));
        
        // Check if user has access to this size (via business ownership)
        User currentUser = securityUtils.getCurrentUser();
        if (currentUser.getBusinessId() == null) {
            throw new ValidationException("User is not associated with any business");
        }
        
        // Load product to check business ownership
        Product product = findProductByIdWithDetails(productSize.getProductId());
        if (!product.getBusinessId().equals(currentUser.getBusinessId())) {
            throw new ValidationException("You can only reset promotions for your own business products");
        }
        
        // Store original promotion info for response
        boolean hadPromotion = productSize.isPromotionActive();
        String originalPromotionType = productSize.getPromotionType() != null ? 
                productSize.getPromotionType().name() : null;
        
        // Reset the promotion
        productSize.removePromotion();
        ProductSize savedSize = productSizeRepository.save(productSize);
        
        Map<String, Object> result = new HashMap<>();
        result.put("sizeId", sizeId);
        result.put("productId", product.getId());
        result.put("productName", product.getName());
        result.put("sizeName", productSize.getName());
        result.put("businessId", currentUser.getBusinessId());
        result.put("hadPromotion", hadPromotion);
        result.put("originalPromotionType", originalPromotionType);
        result.put("timestamp", LocalDateTime.now());
        result.put("message", String.format("Reset promotion for size '%s' of product '%s'", 
                productSize.getName(), product.getName()));
        
        log.info("Reset promotion for size {} ({}) of product {} for business {}", 
                sizeId, productSize.getName(), product.getName(), currentUser.getBusinessId());
        
        return result;
    }

    @Override
    public Map<String, Object> setFavoriteStatus(UUID productId, boolean favorite) {
        User currentUser = securityUtils.getCurrentUser();
        boolean isFavorited = productFavoriteRepository.existsByUserIdAndProductId(currentUser.getId(), productId);

        Map<String, Object> result = new HashMap<>();
        result.put("productId", productId);
        result.put("userId", currentUser.getId());
        result.put("requestedStatus", favorite);
        result.put("timestamp", LocalDateTime.now());

        if (favorite && !isFavorited) {
            // Add to favorites
            ProductFavorite favoriteEntity = new ProductFavorite(currentUser.getId(), productId);
            productFavoriteRepository.save(favoriteEntity);
            productRepository.incrementFavoriteCount(productId);
            result.put("action", "added");
            result.put("isFavorited", true);
            log.info("Added product {} to favorites for user: {}", productId, currentUser.getId());
        } else if (!favorite && isFavorited) {
            // Remove from favorites
            productFavoriteRepository.deleteByUserIdAndProductId(currentUser.getId(), productId);
            productRepository.decrementFavoriteCount(productId);
            result.put("action", "removed");
            result.put("isFavorited", false);
            log.info("Removed product {} from favorites for user: {}", productId, currentUser.getId());
        } else {
            // No change needed
            result.put("action", "unchanged");
            result.put("isFavorited", isFavorited);
            result.put("message", favorite ? "Product is already in favorites" : "Product is not in favorites");
        }

        return result;
    }

    @Override
    public void addToFavorites(UUID productId) {
        User currentUser = securityUtils.getCurrentUser();

        // Check if already favorited
        if (productFavoriteRepository.existsByUserIdAndProductId(currentUser.getId(), productId)) {
            throw new ValidationException("Product is already in your favorites");
        }

        // Create favorite
        ProductFavorite favorite = new ProductFavorite(currentUser.getId(), productId);
        productFavoriteRepository.save(favorite);

        // Increment product favorite count
        productRepository.incrementFavoriteCount(productId);

        log.info("Added product {} to favorites for user: {}", productId, currentUser.getId());
    }

    @Override
    public void removeFromFavorites(UUID productId) {
        User currentUser = securityUtils.getCurrentUser();

        // Check if favorited
        if (!productFavoriteRepository.existsByUserIdAndProductId(currentUser.getId(), productId)) {
            throw new ValidationException("Product is not in your favorites");
        }

        // Remove favorite
        productFavoriteRepository.deleteByUserIdAndProductId(currentUser.getId(), productId);

        // Decrement product favorite count
        productRepository.decrementFavoriteCount(productId);

        log.info("Removed product {} from favorites for user: {}", productId, currentUser.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<ProductResponse> getUserFavorites(ProductFilterRequest filter) {
        UUID userId = securityUtils.getCurrentUserId();

        // Create specification for favorite products
        Specification<ProductFavorite> favoriteSpec = (root, query, criteriaBuilder) -> {
            query.distinct(true);
            return criteriaBuilder.and(
                criteriaBuilder.equal(root.get("userId"), userId),
                criteriaBuilder.equal(root.get("isDeleted"), false)
            );
        };

        // Create pageable
        int pageNo = filter.getPageNo() != null && filter.getPageNo() > 0 ? filter.getPageNo() - 1 : 0;
        Pageable pageable = PaginationUtils.createPageable(
                pageNo, filter.getPageSize(), filter.getSortBy(), filter.getSortDirection()
        );

        // Get paginated favorites
        Page<ProductFavorite> favoritePage = productFavoriteRepository.findAll(favoriteSpec, pageable);
        
        // Extract products and map to responses
        List<Product> products = favoritePage.getContent().stream()
                .map(ProductFavorite::getProduct)
                .toList();
        
        List<ProductResponse> responses = productMapper.toResponseList(products);
        
        // Set all as favorite
        responses.forEach(response -> response.setIsFavorited(true));

        // Create proper pagination response
        return PaginationResponse.<ProductResponse>builder()
                .content(responses)
                .pageNo(favoritePage.getNumber() + 1)
                .pageSize(favoritePage.getSize())
                .totalElements(favoritePage.getTotalElements())
                .totalPages(favoritePage.getTotalPages())
                .first(favoritePage.isFirst())
                .last(favoritePage.isLast())
                .hasNext(favoritePage.hasNext())
                .hasPrevious(favoritePage.hasPrevious())
                .build();
    }

    @Override
    public Map<String, Object> removeAllFavorites() {
        UUID userId = securityUtils.getCurrentUserId();
        
        // Get all favorite product IDs for decrementing counts
        List<ProductFavorite> favorites = productFavoriteRepository.findFavoritesByUserId(userId);
        List<UUID> productIds = favorites.stream()
                .map(ProductFavorite::getProductId)
                .toList();

        // Delete all favorites for user
        int removedCount = productFavoriteRepository.deleteAllByUserId(userId);

        // Decrement favorite counts for all products
        productIds.forEach(productRepository::decrementFavoriteCount);

        Map<String, Object> result = new HashMap<>();
        result.put("removedCount", removedCount);
        result.put("userId", userId);
        result.put("timestamp", LocalDateTime.now());
        result.put("message", String.format("Removed %d products from favorites", removedCount));

        log.info("Removed all {} favorites for user: {}", removedCount, userId);
        
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getFavoriteCount() {
        UUID userId = securityUtils.getCurrentUserId();
        
        long favoriteCount = productFavoriteRepository.countByUserId(userId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("favoriteCount", favoriteCount);
        result.put("userId", userId);
        result.put("timestamp", LocalDateTime.now());
        
        return result;
    }

    // ================================
    // UNIFIED PROMOTION MANAGEMENT
    // ================================

    @Override
    public Map<String, Object> resetProductPromotion(UUID productId) {
        Product product = findProductByIdWithDetails(productId);
        
        // Check business ownership
        User currentUser = securityUtils.getCurrentUser();
        if (currentUser.getBusinessId() == null) {
            throw new ValidationException("User is not associated with any business");
        }
        if (!product.getBusinessId().equals(currentUser.getBusinessId())) {
            throw new ValidationException("You can only reset promotions for your own business products");
        }
        
        // Track what was reset
        boolean productHadPromotion = product.isPromotionActive();
        int sizesWithPromotions = 0;
        
        // Reset product-level promotion
        product.removePromotion();
        
        // Reset all size-level promotions
        if (product.getSizes() != null && !product.getSizes().isEmpty()) {
            for (ProductSize size : product.getSizes()) {
                if (size.isPromotionActive()) {
                    sizesWithPromotions++;
                    size.removePromotion();
                }
            }
            productSizeRepository.saveAll(product.getSizes());
        }
        
        Product savedProduct = productRepository.save(product);
        
        Map<String, Object> result = new HashMap<>();
        result.put("productId", productId);
        result.put("productName", product.getName());
        result.put("businessId", currentUser.getBusinessId());
        result.put("productHadPromotion", productHadPromotion);
        result.put("sizesWithPromotions", sizesWithPromotions);
        result.put("totalPromotionsReset", (productHadPromotion ? 1 : 0) + sizesWithPromotions);
        result.put("timestamp", LocalDateTime.now());
        result.put("message", String.format("Reset all promotions for product '%s' (%d total)", 
                product.getName(), (productHadPromotion ? 1 : 0) + sizesWithPromotions));
        
        log.info("Reset all promotions for product {} - Product: {}, Sizes: {}", 
                productId, productHadPromotion, sizesWithPromotions);
        
        return result;
    }

    @Override
    public Map<String, Object> resetSizePromotion(UUID productId, UUID sizeId) {
        // Find and validate the product first
        Product product = findProductByIdWithDetails(productId);
        
        // Check business ownership
        User currentUser = securityUtils.getCurrentUser();
        if (currentUser.getBusinessId() == null) {
            throw new ValidationException("User is not associated with any business");
        }
        if (!product.getBusinessId().equals(currentUser.getBusinessId())) {
            throw new ValidationException("You can only reset promotions for your own business products");
        }
        
        // Find the specific size within this product
        ProductSize productSize = product.getSizes().stream()
                .filter(size -> size.getId().equals(sizeId))
                .findFirst()
                .orElseThrow(() -> new ValidationException("Size not found in this product"));
        
        // Store original promotion info for response
        boolean hadPromotion = productSize.isPromotionActive();
        String originalPromotionType = productSize.getPromotionType() != null ? 
                productSize.getPromotionType().name() : null;
        
        // Reset the promotion
        productSize.removePromotion();
        ProductSize savedSize = productSizeRepository.save(productSize);
        
        Map<String, Object> result = new HashMap<>();
        result.put("productId", productId);
        result.put("sizeId", sizeId);
        result.put("productName", product.getName());
        result.put("sizeName", productSize.getName());
        result.put("businessId", currentUser.getBusinessId());
        result.put("hadPromotion", hadPromotion);
        result.put("originalPromotionType", originalPromotionType);
        result.put("timestamp", LocalDateTime.now());
        result.put("message", String.format("Reset promotion for size '%s' of product '%s'", 
                productSize.getName(), product.getName()));
        
        log.info("Reset promotion for size {} ({}) of product {} for business {}", 
                sizeId, productSize.getName(), product.getName(), currentUser.getBusinessId());
        
        return result;
    }

    @Override
    public Map<String, Object> resetExpiredPromotions() {
        LocalDateTime now = LocalDateTime.now();
        
        // Reset expired product promotions
        int productPromotionsReset = productRepository.clearExpiredPromotions(now);
        
        // Reset expired size promotions
        int sizePromotionsReset = productSizeRepository.clearExpiredPromotions(now);
        
        Map<String, Object> result = new HashMap<>();
        result.put("productPromotionsReset", productPromotionsReset);
        result.put("sizePromotionsReset", sizePromotionsReset);
        result.put("totalReset", productPromotionsReset + sizePromotionsReset);
        result.put("timestamp", now);
        result.put("message", String.format("Reset %d expired promotions", productPromotionsReset + sizePromotionsReset));
        
        log.info("Reset {} expired promotions ({} products, {} sizes)", 
                productPromotionsReset + sizePromotionsReset, productPromotionsReset, sizePromotionsReset);
        
        return result;
    }

    @Override
    public Map<String, Object> resetAllBusinessPromotions() {
        User currentUser = securityUtils.getCurrentUser();
        if (currentUser.getBusinessId() == null) {
            throw new ValidationException("User is not associated with any business");
        }

        // Reset all product promotions for business
        int productPromotionsReset = productRepository.clearAllPromotionsForBusiness(currentUser.getBusinessId());
        
        // Reset all size promotions for business products
        int sizePromotionsReset = productSizeRepository.clearAllPromotionsForBusiness(currentUser.getBusinessId());
        
        Map<String, Object> result = new HashMap<>();
        result.put("businessId", currentUser.getBusinessId());
        result.put("productPromotionsReset", productPromotionsReset);
        result.put("sizePromotionsReset", sizePromotionsReset);
        result.put("totalReset", productPromotionsReset + sizePromotionsReset);
        result.put("timestamp", LocalDateTime.now());
        result.put("message", String.format("Reset all %d promotions for business", productPromotionsReset + sizePromotionsReset));
        
        log.info("Reset all {} promotions for business {} ({} products, {} sizes)", 
                productPromotionsReset + sizePromotionsReset, currentUser.getBusinessId(), 
                productPromotionsReset, sizePromotionsReset);
        
        return result;
    }

    // ================================
    // PRIVATE HELPER METHODS
    // ================================

    private Product findProductByIdWithDetails(UUID id) {
        return productRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));
    }

    private void createProductSizes(UUID productId, List<ProductSizeRequest> sizeRequests) {
        if (sizeRequests == null || sizeRequests.isEmpty()) {
            return;
        }

        for (ProductSizeRequest sizeRequest : sizeRequests) {
            ProductSize productSize = productSizeMapper.toEntity(sizeRequest);
            productSize.setProductId(productId);

            // Set promotion type enum
            if (sizeRequest.getPromotionType() != null) {
                try {
                    productSize.setPromotionType(PromotionType.valueOf(sizeRequest.getPromotionType().toUpperCase()));
                } catch (IllegalArgumentException e) {
                    throw new ValidationException("Invalid promotion type: " + sizeRequest.getPromotionType());
                }
            }

            productSizeRepository.save(productSize);
        }
    }

    private void createProductImages(UUID productId, List<ProductImageRequest> imageRequests) {
        if (imageRequests == null || imageRequests.isEmpty()) {
            return;
        }

        boolean hasMainSet = false;

        for (ProductImageRequest imageRequest : imageRequests) {
            ProductImage productImage = productImageMapper.toEntity(imageRequest);
            productImage.setProductId(productId);

            // Ensure only one main image
            if ("MAIN".equalsIgnoreCase(imageRequest.getImageType())) {
                if (!hasMainSet) {
                    productImage.setAsMain();
                    hasMainSet = true;
                } else {
                    productImage.setAsGallery();
                }
            }

            productImageRepository.save(productImage);
        }

        // If no main was set, set the first one as main
        if (!hasMainSet) {
            List<ProductImage> images = productImageRepository.findByProductIdOrderByMainAndSort(productId);
            if (!images.isEmpty()) {
                ProductImage firstImage = images.get(0);
                firstImage.setAsMain();
                productImageRepository.save(firstImage);
            }
        }
    }

    private void setFavoriteStatusForUser(List<ProductResponse> products) {
        try {
            User currentUser = securityUtils.getCurrentUser();
            for (ProductResponse product : products) {
                boolean isFavorited = productFavoriteRepository.existsByUserIdAndProductId(
                        currentUser.getId(), product.getId());
                product.setIsFavorited(isFavorited);
            }
        } catch (Exception e) {
            // User not logged in, keep all as false
            products.forEach(product -> product.setIsFavorited(false));
        }
    }
}
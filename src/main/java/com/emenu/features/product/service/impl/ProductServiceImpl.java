package com.emenu.features.product.service.impl;

import com.emenu.exception.custom.NotFoundException;
import com.emenu.exception.custom.ValidationException;
import com.emenu.features.auth.models.User;
import com.emenu.features.product.dto.filter.ProductFilterDto;
import com.emenu.features.product.dto.request.ProductCreateDto;
import com.emenu.features.product.dto.request.ProductImageCreateDto;
import com.emenu.features.product.dto.request.ProductSizeCreateDto;
import com.emenu.features.product.dto.response.*;
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
import com.emenu.features.product.specification.ProductSpecifications;
import com.emenu.features.product.utils.ProductUtils;
import com.emenu.security.SecurityUtils;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.mapper.PaginationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductSizeRepository productSizeRepository;
    private final ProductFavoriteRepository favoriteRepository;
    private final ProductMapper productMapper;
    private final ProductImageMapper productImageMapper;
    private final ProductSizeMapper productSizeMapper;
    private final FavoriteMapper favoriteMapper;
    private final PaginationMapper paginationMapper;
    private final SecurityUtils securityUtils;
    private final ProductUtils productUtils;

    // ================================
    // 🚀 FAST LISTING - Using Specifications and Indexes
    // ================================

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<ProductListDto> getAllProducts(ProductFilterDto filter) {
        log.info("Getting products with filter - Page: {}, Size: {}, Business: {}, Search: '{}'", 
                filter.getPageNo(), filter.getPageSize(), filter.getBusinessId(), filter.getSearch());

        try {
            long startTime = System.currentTimeMillis();

            // Apply business filter for authenticated business users
            Optional<User> currentUser = securityUtils.getCurrentUserOptional();
            if (currentUser.isPresent() && currentUser.get().isBusinessUser() && filter.getBusinessId() == null) {
                filter.setBusinessId(currentUser.get().getBusinessId());
                log.debug("Applied business filter for user: {}", currentUser.get().getId());
            }

            // Validate and create pageable (convert from 1-based to 0-based)
            Pageable pageable = createPageable(filter);
            
            // Build specification using our optimized specifications
            Specification<Product> spec = ProductSpecifications.withFilter(filter);
            
            // Execute query with specifications (uses our indexes)
            Page<Product> productPage = productRepository.findAll(spec, pageable);
            
            // Map to DTOs with performance tracking
            long mappingStart = System.currentTimeMillis();
            PaginationResponse<ProductListDto> response = paginationMapper.toPaginationResponse(
                productPage, 
                products -> productMapper.toListDtos(products)
            );
            long mappingTime = System.currentTimeMillis() - mappingStart;
            
            // Enrich with favorite status if user is authenticated
            if (currentUser.isPresent()) {
                long favoriteStart = System.currentTimeMillis();
                response.setContent(
                    productMapper.enrichWithFavorites(response.getContent(), currentUser.get().getId())
                );
                long favoriteTime = System.currentTimeMillis() - favoriteStart;
                log.debug("Favorite enrichment took: {}ms", favoriteTime);
            }

            long totalTime = System.currentTimeMillis() - startTime;
            log.info("Retrieved {} products in {}ms (mapping: {}ms)", 
                    response.getContent().size(), totalTime, mappingTime);
            
            return response;

        } catch (Exception e) {
            log.error("Error retrieving products with filter: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve products", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<ProductListDto> getUserFavorites(ProductFilterDto filter) {
        UUID userId = securityUtils.getCurrentUserId();
        log.info("Getting favorites for user: {} - Page: {}, Size: {}", 
                userId, filter.getPageNo(), filter.getPageSize());

        try {
            Pageable pageable = createPageable(filter);
            
            // Use optimized favorites query
            Page<Product> favoritePage = productRepository.findUserFavorites(userId, pageable);
            
            // Map to DTOs and set all as favorited
            PaginationResponse<ProductListDto> response = paginationMapper.toPaginationResponse(
                favoritePage, 
                products -> productMapper.toListDtos(products)
            );
            
            // All products in favorites are favorited by definition
            response.getContent().forEach(product -> product.setIsFavorited(true));

            log.info("Retrieved {} favorites for user: {}", response.getContent().size(), userId);
            return response;

        } catch (Exception e) {
            log.error("Error retrieving favorites for user {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve favorites", e);
        }
    }

    // ================================
    // 🚀 SINGLE PRODUCT - With Full Details
    // ================================

    @Override
    @Transactional(readOnly = true)
    public ProductDetailDto getProductById(UUID id) {
        log.info("Getting product by ID: {}", id);

        Product product = productRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new NotFoundException("Product not found with ID: " + id));

        // Validate access for business users
        Optional<User> currentUser = securityUtils.getCurrentUserOptional();
        if (currentUser.isPresent() && currentUser.get().isBusinessUser()) {
            validateBusinessAccess(product, currentUser.get());
        }

        ProductDetailDto dto = productMapper.toDetailDto(product);
        
        // Enrich with favorite status if user is authenticated
        if (currentUser.isPresent()) {
            dto = productMapper.enrichWithFavorite(dto, currentUser.get().getId());
        }

        log.debug("Retrieved product: {} for business: {}", product.getName(), product.getBusinessId());
        return dto;
    }

    @Override
    @Transactional
    public ProductDetailDto getProductByIdPublic(UUID id) {
        log.info("Getting product by ID (public): {}", id);

        Product product = productRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new NotFoundException("Product not found with ID: " + id));

        // Only show active products publicly
        if (!product.isActive()) {
            throw new NotFoundException("Product is not available");
        }

        // Increment view count atomically
        try {
            productRepository.incrementViewCount(id);
            log.debug("Incremented view count for product: {}", id);
        } catch (Exception e) {
            log.warn("Failed to increment view count for product {}: {}", id, e.getMessage());
        }
        
        ProductDetailDto dto = productMapper.toDetailDto(product);
        
        // Enrich with favorite status if user is authenticated
        Optional<User> currentUser = securityUtils.getCurrentUserOptional();
        if (currentUser.isPresent()) {
            dto = productMapper.enrichWithFavorite(dto, currentUser.get().getId());
        }

        return dto;
    }

    // ================================
    // 🚀 CRUD OPERATIONS
    // ================================

    @Override
    public ProductDetailDto createProduct(ProductCreateDto request) {
        log.info("Creating product: {}", request.getName());

        // Validate user and business association
        User currentUser = securityUtils.getCurrentUser();
        validateUserBusinessAssociation(currentUser);
        
        // Validate product data
        validateProductCreateRequest(request);

        try {
            // Create main product entity
            Product product = productMapper.toEntity(request);
            product.setBusinessId(currentUser.getBusinessId());
            
            // Set default values
            if (product.getViewCount() == null) product.setViewCount(0L);
            if (product.getFavoriteCount() == null) product.setFavoriteCount(0L);
            
            // Save product first to get ID
            Product savedProduct = productRepository.save(product);
            log.debug("Created product entity with ID: {}", savedProduct.getId());

            // Handle images if provided
            if (request.getImages() != null && !request.getImages().isEmpty()) {
                handleProductImages(savedProduct, request.getImages());
            }

            // Handle sizes if provided
            if (request.getSizes() != null && !request.getSizes().isEmpty()) {
                handleProductSizes(savedProduct, request.getSizes());
            }

            // Log successful creation
            productUtils.logProductOperation("CREATE", savedProduct.getName(), 
                    currentUser.getBusinessId().toString());
            
            log.info("Product created successfully: {} (ID: {}) for business: {}", 
                    savedProduct.getName(), savedProduct.getId(), currentUser.getBusinessId());
            
            // Return full product details
            return getProductById(savedProduct.getId());

        } catch (Exception e) {
            log.error("Error creating product '{}': {}", request.getName(), e.getMessage(), e);
            throw new RuntimeException("Failed to create product: " + e.getMessage(), e);
        }
    }

    @Override
    public ProductDetailDto updateProduct(UUID id, ProductCreateDto request) {
        log.info("Updating product: {} with name: {}", id, request.getName());

        // Find existing product
        Product product = productRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Product not found with ID: " + id));

        // Validate business ownership
        User currentUser = securityUtils.getCurrentUser();
        validateBusinessOwnership(product, currentUser);
        
        // Validate update request
        validateProductCreateRequest(request);

        try {
            // Update basic product fields
            updateProductFromDto(product, request);
            
            // Save updated product
            Product updatedProduct = productRepository.save(product);
            
            // Handle images update
            if (request.getImages() != null) {
                updateProductImages(updatedProduct, request.getImages());
            }
            
            // Handle sizes update
            if (request.getSizes() != null) {
                updateProductSizes(updatedProduct, request.getSizes());
            }

            // Log successful update
            productUtils.logProductOperation("UPDATE", updatedProduct.getName(), 
                    currentUser.getBusinessId().toString());
            
            log.info("Product updated successfully: {} (ID: {})", updatedProduct.getName(), id);
            
            // Return updated product details
            return getProductById(updatedProduct.getId());

        } catch (Exception e) {
            log.error("Error updating product {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to update product: " + e.getMessage(), e);
        }
    }

    @Override
    public ProductDetailDto deleteProduct(UUID id) {
        log.info("Deleting product: {}", id);

        Product product = productRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Product not found with ID: " + id));

        // Validate business ownership
        User currentUser = securityUtils.getCurrentUser();
        validateBusinessOwnership(product, currentUser);

        try {
            // Soft delete the product
            product.softDelete();
            Product deletedProduct = productRepository.save(product);

            // Log successful deletion
            productUtils.logProductOperation("DELETE", deletedProduct.getName(), 
                    currentUser.getBusinessId().toString());

            log.info("Product deleted successfully: {} (ID: {})", deletedProduct.getName(), id);
            
            // Return the deleted product details
            return productMapper.toDetailDto(deletedProduct);

        } catch (Exception e) {
            log.error("Error deleting product {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to delete product: " + e.getMessage(), e);
        }
    }

    // ================================
    // 🚀 FAVORITES MANAGEMENT
    // ================================

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

        try {
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
                log.debug("Added product {} to favorites for user: {}", productId, userId);
            } else {
                // Remove from favorites
                favoriteRepository.deleteByUserIdAndProductId(userId, productId);
                productRepository.decrementFavoriteCount(productId);
                action = "removed";
                finalStatus = false;
                log.debug("Removed product {} from favorites for user: {}", productId, userId);
            }

            FavoriteToggleDto result = favoriteMapper.createToggleResponse(productId, userId, finalStatus, action);
            log.info("Favorite toggle completed: {} product {} for user {}", action, productId, userId);
            
            return result;

        } catch (Exception e) {
            log.error("Error toggling favorite for product {} by user {}: {}", productId, userId, e.getMessage(), e);
            throw new RuntimeException("Failed to toggle favorite: " + e.getMessage(), e);
        }
    }

    @Override
    public FavoriteRemoveAllDto removeAllFavorites() {
        UUID userId = securityUtils.getCurrentUserId();
        log.info("Removing all favorites for user: {}", userId);

        try {
            // Get count before deletion for response
            long favoriteCount = favoriteRepository.countByUserIdAndIsDeletedFalse(userId);
            
            // Remove all favorites for the user
            int removedCount = favoriteRepository.deleteAllByUserId(userId);
            
            // Note: We're not updating product favorite counts here for performance
            // This could be handled by a background job or scheduled task
            
            log.info("Removed {} favorites for user: {} (expected: {})", removedCount, userId, favoriteCount);
            
            FavoriteRemoveAllDto result = FavoriteRemoveAllDto.builder()
                    .userId(userId)
                    .removedCount(removedCount)
                    .timestamp(LocalDateTime.now())
                    .message(String.format("Removed %d products from favorites", removedCount))
                    .build();

            return result;

        } catch (Exception e) {
            log.error("Error removing all favorites for user {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Failed to remove all favorites: " + e.getMessage(), e);
        }
    }

    // ================================
    // 🚀 HELPER METHODS - Product Creation/Update
    // ================================

    private void handleProductImages(Product product, List<ProductImageCreateDto> imageDtos) {
        if (imageDtos == null || imageDtos.isEmpty()) {
            return;
        }

        log.debug("Processing {} images for product: {}", imageDtos.size(), product.getId());

        List<ProductImage> images = new ArrayList<>();
        boolean hasMainImage = false;

        for (int i = 0; i < imageDtos.size(); i++) {
            ProductImageCreateDto imageDto = imageDtos.get(i);
            
            // Validate image URL
            if (!productUtils.isValidImageUrl(imageDto.getImageUrl())) {
                log.warn("Invalid image URL skipped: {}", imageDto.getImageUrl());
                continue;
            }

            ProductImage image = productImageMapper.toEntity(imageDto);
            image.setProductId(product.getId());
            
            // Set first image as main if no main image specified
            if (i == 0 && !hasMainImage && "GALLERY".equals(imageDto.getImageType())) {
                image.setImageType(com.emenu.enums.product.ImageType.MAIN);
                hasMainImage = true;
            }
            
            images.add(image);
        }

        if (!images.isEmpty()) {
            productImageRepository.saveAll(images);
            log.debug("Saved {} images for product: {}", images.size(), product.getId());
        }
    }

    private void handleProductSizes(Product product, List<ProductSizeCreateDto> sizeDtos) {
        if (sizeDtos == null || sizeDtos.isEmpty()) {
            return;
        }

        log.debug("Processing {} sizes for product: {}", sizeDtos.size(), product.getId());

        List<ProductSize> sizes = sizeDtos.stream()
                .map(sizeDto -> {
                    // Validate size data
                    if (!productUtils.isValidPrice(sizeDto.getPrice())) {
                        throw new ValidationException("Invalid price for size: " + sizeDto.getName());
                    }
                    
                    ProductSize size = productSizeMapper.toEntity(sizeDto);
                    size.setProductId(product.getId());
                    return size;
                })
                .collect(Collectors.toList());

        productSizeRepository.saveAll(sizes);
        log.debug("Saved {} sizes for product: {}", sizes.size(), product.getId());
    }

    private void updateProductImages(Product product, List<ProductImageCreateDto> imageDtos) {
        // Simple implementation: delete all existing and create new ones
        // In production, you might want more sophisticated update logic
        
        List<ProductImage> existingImages = productImageRepository.findByProductIdOrderByMainAndSort(product.getId());
        if (!existingImages.isEmpty()) {
            productImageRepository.deleteAll(existingImages);
            log.debug("Deleted {} existing images for product: {}", existingImages.size(), product.getId());
        }
        
        handleProductImages(product, imageDtos);
    }

    private void updateProductSizes(Product product, List<ProductSizeCreateDto> sizeDtos) {
        // Simple implementation: delete all existing and create new ones
        // In production, you might want more sophisticated update logic
        
        List<ProductSize> existingSizes = product.getSizes();
        if (existingSizes != null && !existingSizes.isEmpty()) {
            productSizeRepository.deleteAll(existingSizes);
            log.debug("Deleted {} existing sizes for product: {}", existingSizes.size(), product.getId());
        }
        
        handleProductSizes(product, sizeDtos);
    }

    // ================================
    // 🚀 HELPER METHODS - Validation
    // ================================

    private void validateProductCreateRequest(ProductCreateDto request) {
        // Validate product name
        if (!productUtils.isValidProductName(request.getName())) {
            throw new ValidationException("Invalid product name: " + request.getName());
        }

        // Validate description
        if (!productUtils.isValidDescription(request.getDescription())) {
            throw new ValidationException("Product description is too long");
        }

        // Validate price
        if (request.getPrice() != null && !productUtils.isValidPrice(request.getPrice())) {
            throw new ValidationException("Invalid product price: " + request.getPrice());
        }

        // Validate promotion if present
        if (StringUtils.hasText(request.getPromotionType()) && request.getPromotionValue() != null) {
            try {
                com.emenu.enums.product.PromotionType promotionType = 
                    com.emenu.enums.product.PromotionType.valueOf(request.getPromotionType().toUpperCase());
                
                if (!productUtils.isValidPromotion(promotionType, request.getPromotionValue())) {
                    throw new ValidationException("Invalid promotion configuration");
                }
                
                if (!productUtils.isValidPromotionDateRange(request.getPromotionFromDate(), request.getPromotionToDate())) {
                    throw new ValidationException("Invalid promotion date range");
                }
            } catch (IllegalArgumentException e) {
                throw new ValidationException("Invalid promotion type: " + request.getPromotionType());
            }
        }

        // Validate category
        if (request.getCategoryId() == null) {
            throw new ValidationException("Product category is required");
        }

        log.debug("Product create request validation passed for: {}", request.getName());
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

    private void validateBusinessAccess(Product product, User user) {
        // Business users can only access their own products unless they're platform users
        if (user.isBusinessUser() && !product.getBusinessId().equals(user.getBusinessId())) {
            throw new ValidationException("Access denied to product from different business");
        }
    }

    // ================================
    // 🚀 HELPER METHODS - Data Manipulation
    // ================================

    private void updateProductFromDto(Product product, ProductCreateDto dto) {
        // Update basic fields
        product.setName(productUtils.sanitizeProductName(dto.getName()));
        product.setDescription(dto.getDescription());
        product.setCategoryId(dto.getCategoryId());
        product.setBrandId(dto.getBrandId());
        product.setPrice(dto.getPrice());
        product.setStatus(dto.getStatus());
        
        // Update promotion fields
        if (StringUtils.hasText(dto.getPromotionType()) && dto.getPromotionValue() != null) {
            try {
                product.setPromotionType(
                    com.emenu.enums.product.PromotionType.valueOf(dto.getPromotionType().toUpperCase())
                );
                product.setPromotionValue(dto.getPromotionValue());
                product.setPromotionFromDate(dto.getPromotionFromDate());
                product.setPromotionToDate(dto.getPromotionToDate());
                log.debug("Updated promotion for product: {}", product.getName());
            } catch (IllegalArgumentException e) {
                // Clear promotion if type is invalid
                clearProductPromotion(product);
                log.warn("Invalid promotion type '{}', cleared promotion for product: {}", 
                        dto.getPromotionType(), product.getName());
            }
        } else {
            // Clear promotion if not provided
            clearProductPromotion(product);
        }
        
        log.debug("Updated product fields for: {}", product.getName());
    }

    private void clearProductPromotion(Product product) {
        product.setPromotionType(null);
        product.setPromotionValue(null);
        product.setPromotionFromDate(null);
        product.setPromotionToDate(null);
    }

    // ================================
    // 🚀 HELPER METHODS - Pagination and Sorting
    // ================================

    private Pageable createPageable(ProductFilterDto filter) {
        // Validate and convert page number (1-based to 0-based)
        int pageNo = Math.max(0, (filter.getPageNo() != null ? filter.getPageNo() : 1) - 1);
        int pageSize = Math.min(100, Math.max(1, filter.getPageSize() != null ? filter.getPageSize() : 10));
        
        // Validate and create sort
        Sort.Direction direction = "ASC".equalsIgnoreCase(filter.getSortDirection()) 
                ? Sort.Direction.ASC 
                : Sort.Direction.DESC;
        
        String sortBy = validateSortField(filter.getSortBy());
        Sort sort = Sort.by(direction, sortBy);
        
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        log.debug("Created pageable: page={}, size={}, sort={}_{}", pageNo, pageSize, sortBy, direction);
        
        return pageable;
    }

    private String validateSortField(String sortBy) {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            return "createdAt";
        }
        
        // Validate against allowed sort fields to prevent SQL injection
        return switch (sortBy.toLowerCase()) {
            case "name" -> "name";
            case "price" -> "price";
            case "createdat", "created_at" -> "createdAt";
            case "updatedat", "updated_at" -> "updatedAt";
            case "viewcount", "view_count" -> "viewCount";
            case "favoritecount", "favorite_count" -> "favoriteCount";
            case "status" -> "status";
            default -> {
                log.warn("Invalid sort field '{}', using default 'createdAt'", sortBy);
                yield "createdAt";
            }
        };
    }

    // ================================
    // 🚀 UTILITY METHODS
    // ================================

    private void logPerformanceMetrics(String operation, long startTime, int resultCount) {
        long duration = System.currentTimeMillis() - startTime;
        log.info("Performance - {}: {}ms for {} results (avg: {}ms/item)", 
                operation, duration, resultCount, 
                resultCount > 0 ? duration / resultCount : 0);
    }
}
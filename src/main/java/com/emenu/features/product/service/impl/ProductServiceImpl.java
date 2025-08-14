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
import com.emenu.features.product.specification.ProductSpecification;
import com.emenu.security.SecurityUtils;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.pagination.PaginationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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
    private final ProductMapper productMapper;
    private final ProductSizeMapper productSizeMapper;
    private final ProductImageMapper productImageMapper;
    private final FavoriteResponseMapper favoriteMapper;
    private final PromotionResponseMapper promotionMapper;
    private final SecurityUtils securityUtils;

    // ================================
    // CRUD OPERATIONS
    // ================================

    @Override
    public ProductResponse createProduct(ProductCreateRequest request) {
        log.info("Creating product: {}", request.getName());

        User currentUser = securityUtils.getCurrentUser();
        validateUserBusinessAssociation(currentUser);

        // Create product entity
        Product product = productMapper.toEntity(request);
        product.setBusinessId(currentUser.getBusinessId());
        Product savedProduct = productRepository.save(product);

        // Handle sizes with enhanced mapper
        if (request.getSizes() != null && !request.getSizes().isEmpty()) {
            ProductSizeMapper.SizeCreationResult sizeResult = 
                    productSizeMapper.processSizeCreation(request.getSizes(), savedProduct.getId());
            productSizeRepository.saveAll(sizeResult.sizes);
        }

        // Handle images with enhanced mapper
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            ProductImageMapper.ImageCreationResult imageResult = 
                    productImageMapper.processImageCreation(request.getImages(), savedProduct.getId());
            productImageRepository.saveAll(imageResult.images);
        }

        log.info("Product created successfully: {} for business: {}", 
                savedProduct.getName(), currentUser.getBusinessId());
        
        return getProductById(savedProduct.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<ProductResponse> getAllProducts(ProductFilterRequest filter) {
        User currentUser = securityUtils.getCurrentUser();
        if (currentUser.isBusinessUser() && filter.getBusinessId() == null) {
            filter.setBusinessId(currentUser.getBusinessId());
        }

        Specification<Product> spec = ProductSpecification.buildSpecification(filter);
        Pageable pageable = createPageable(filter);
        Page<Product> productPage = productRepository.findAll(spec, pageable);
        
        // Load collections and convert to response
        List<Product> productsWithCollections = productPage.getContent().stream()
                .map(this::loadProductCollections)
                .toList();

        // Use enhanced mapper to convert to response with sorting
        List<ProductResponse> responses = productsWithCollections.stream()
                .map(productMapper::toResponse)
                .toList();

        // Enrich with user-specific data
        responses = productMapper.enrichWithUserData(responses, this::isProductFavorited);

        PaginationResponse<ProductResponse> paginationResponse = productMapper.toPaginationResponse(productPage);
        paginationResponse.setContent(responses);
        return paginationResponse;
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(UUID id) {
        Product product = findProductByIdWithDetails(id);
        ProductResponse response = productMapper.toResponse(product);
        return productMapper.enrichWithUserData(response, this::isProductFavorited);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductByIdPublic(UUID id) {
        Product product = findProductByIdWithDetails(id);
        productRepository.incrementViewCount(id);
        ProductResponse response = productMapper.toResponse(product);
        return productMapper.enrichWithUserData(response, this::isProductFavorited);
    }

    @Override
    public ProductResponse updateProduct(UUID id, ProductUpdateRequest request) {
        log.info("Updating product: {}", id);

        Product product = productRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        User currentUser = securityUtils.getCurrentUser();
        validateBusinessOwnership(product, currentUser);

        // Update sizes with enhanced mapper
        if (request.getSizes() != null) {
            List<ProductSize> existingSizes = loadProductSizes(product.getId());
            ProductSizeMapper.SizeUpdateResult sizeResult = 
                    productSizeMapper.processSizeUpdate(request.getSizes(), existingSizes, product.getId());
            
            // Delete removed sizes
            if (!sizeResult.sizesToDelete.isEmpty()) {
                sizeResult.sizesToDelete.forEach(sizeId -> 
                        productSizeRepository.findById(sizeId).ifPresent(productSizeRepository::delete));
            }
            
            // Save updated/new sizes
            productSizeRepository.saveAll(sizeResult.sizes);
            productSizeRepository.flush();
        }

        // Update images with enhanced mapper
        if (request.getImages() != null) {
            List<ProductImage> existingImages = loadProductImages(product.getId());
            ProductImageMapper.ImageUpdateResult imageResult = 
                    productImageMapper.processImageUpdate(request.getImages(), existingImages, product.getId());
            
            // Delete removed images
            if (!imageResult.imagesToDelete.isEmpty()) {
                imageResult.imagesToDelete.forEach(imageId -> 
                        productImageRepository.findById(imageId).ifPresent(productImageRepository::delete));
            }
            
            // Save updated/new images
            productImageRepository.saveAll(imageResult.images);
            productImageRepository.flush();
        }

        // Update product basic fields
        productMapper.updateEntity(request, product);
        Product updatedProduct = productRepository.save(product);
        productRepository.flush();

        log.info("Product updated successfully: {}", id);
        return getProductById(updatedProduct.getId());
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
    // PROMOTION MANAGEMENT
    // ================================

    @Override
    public ProductPromotionResetResponse resetProductPromotion(UUID productId) {
        Product product = findProductByIdWithDetails(productId);
        User currentUser = securityUtils.getCurrentUser();
        
        validateUserBusinessAssociation(currentUser);
        validateBusinessOwnership(product, currentUser);
        
        boolean productHadPromotion = product.isPromotionActive();
        
        // Reset size promotions using enhanced mapper
        List<ProductSize> sizes = loadProductSizes(productId);
        long sizesWithPromotions = productSizeMapper.countActivePromotions(sizes);
        productSizeMapper.removeAllPromotions(sizes);
        if (!sizes.isEmpty()) {
            productSizeRepository.saveAll(sizes);
        }
        
        // Reset product-level promotion
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
        
        // Remove promotion using mapper utility
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
    // FAVORITES MANAGEMENT  
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
    @Transactional(readOnly = true)
    public PaginationResponse<ProductResponse> getUserFavorites(ProductFilterRequest filter) {
        UUID userId = securityUtils.getCurrentUserId();

        Specification<ProductFavorite> favoriteSpec = (root, query, criteriaBuilder) -> {
            query.distinct(true);
            return criteriaBuilder.and(
                criteriaBuilder.equal(root.get("userId"), userId),
                criteriaBuilder.equal(root.get("isDeleted"), false)
            );
        };

        Pageable pageable = createPageable(filter);
        Page<ProductFavorite> favoritePage = productFavoriteRepository.findAll(favoriteSpec, pageable);
        
        List<Product> products = favoritePage.getContent().stream()
                .map(ProductFavorite::getProduct)
                .map(this::loadProductCollections)
                .toList();
        
        List<ProductResponse> responses = products.stream()
                .map(productMapper::toResponse)
                .peek(response -> response.setIsFavorited(true))
                .toList();

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
    // HELPER METHODS
    // ================================

    private Product findProductByIdWithDetails(UUID id) {
        Product product = productRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));
        return loadProductCollections(product);
    }

    private Product loadProductCollections(Product product) {
        // Load images with sorting
        List<ProductImage> images = loadProductImages(product.getId());
        product.setImages(productImageMapper.getSortedImages(images));
        
        // Load sizes with sorting
        List<ProductSize> sizes = loadProductSizes(product.getId());
        product.setSizes(productSizeMapper.getSortedSizes(sizes));
        
        return product;
    }

    private List<ProductImage> loadProductImages(UUID productId) {
        return productImageRepository.findByProductIdOrderByMainAndSort(productId);
    }

    private List<ProductSize> loadProductSizes(UUID productId) {
        return productRepository.findByIdWithSizes(productId)
                .map(Product::getSizes)
                .orElse(List.of());
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

    private Pageable createPageable(ProductFilterRequest filter) {
        int pageNo = filter.getPageNo() != null && filter.getPageNo() > 0 ? filter.getPageNo() - 1 : 0;
        return PaginationUtils.createPageable(
                pageNo, filter.getPageSize(), filter.getSortBy(), filter.getSortDirection()
        );
    }

    private boolean isProductFavorited(UUID productId) {
        try {
            User currentUser = securityUtils.getCurrentUser();
            return productFavoriteRepository.existsByUserIdAndProductId(
                    currentUser.getId(), productId);
        } catch (Exception e) {
            return false;
        }
    }
}
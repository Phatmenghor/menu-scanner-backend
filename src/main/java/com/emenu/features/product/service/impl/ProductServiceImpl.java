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
    @Transactional(readOnly = true)
    public PaginationResponse<ProductResponse> getAllProducts(ProductFilterRequest filter) {
        log.info("Getting all products - Auth context: {}", securityUtils.getAuthenticationContext());

        Optional<User> currentUser = securityUtils.getCurrentUserOptional();
        
        return productMapper.getProductsWithFilter(
            filter, 
            currentUser, 
            this::loadProductCollections
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(UUID id) {
        log.info("Getting product by ID: {} - Auth context: {}", id, securityUtils.getAuthenticationContext());

        Product product = findProductByIdWithDetails(id);
        ProductResponse response = productMapper.toResponse(product);
        
        Optional<User> currentUser = securityUtils.getCurrentUserOptional();
        if (currentUser.isPresent()) {
            response = productMapper.enrichWithFavoriteStatus(response, currentUser.get().getId());
        } else {
            response.setIsFavorited(false);
        }
        
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductByIdPublic(UUID id) {
        log.info("Getting product by ID (public): {} - Auth context: {}", id, securityUtils.getAuthenticationContext());

        Product product = findProductByIdWithDetails(id);
        productRepository.incrementViewCount(id);
        
        ProductResponse response = productMapper.toResponse(product);
        
        Optional<User> currentUser = securityUtils.getCurrentUserOptional();
        if (currentUser.isPresent()) {
            response = productMapper.enrichWithFavoriteStatus(response, currentUser.get().getId());
        } else {
            response.setIsFavorited(false);
        }
        
        return response;
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
        
        return productMapper.getUserFavoritesWithPagination(
            filter, 
            userId, 
            this::loadProductCollections
        );
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
    // PROMOTION MANAGEMENT
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
    // HELPER METHODS
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
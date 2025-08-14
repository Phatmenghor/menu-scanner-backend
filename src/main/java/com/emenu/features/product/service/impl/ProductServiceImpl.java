package com.emenu.features.product.service.impl;

import com.emenu.enums.product.PromotionType;
import com.emenu.exception.custom.NotFoundException;
import com.emenu.exception.custom.ValidationException;
import com.emenu.features.auth.models.User;
import com.emenu.features.product.dto.filter.ProductFilterRequest;
import com.emenu.features.product.dto.request.ProductCreateRequest;
import com.emenu.features.product.dto.request.ProductImageRequest;
import com.emenu.features.product.dto.request.ProductSizeRequest;
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
import java.util.stream.Collectors;

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

    @Override
    public ProductResponse createProduct(ProductCreateRequest request) {
        log.info("Creating product: {}", request.getName());

        User currentUser = securityUtils.getCurrentUser();
        validateUserBusinessAssociation(currentUser);

        Product product = productMapper.toEntity(request);
        product.setBusinessId(currentUser.getBusinessId());

        Product savedProduct = productRepository.save(product);
        createProductSizes(savedProduct.getId(), request.getSizes());
        createProductImages(savedProduct.getId(), request.getImages());

        log.info("Product created successfully: {} for business: {}", savedProduct.getName(), currentUser.getBusinessId());
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
        List<Product> productsWithCollections = productPage.getContent().stream()
                .map(this::loadProductCollections)
                .toList();

        PaginationResponse<ProductResponse> response = productMapper.toPaginationResponse(productPage);
        setFavoriteStatusForUser(response.getContent());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(UUID id) {
        Product product = findProductByIdWithDetails(id);
        ProductResponse response = productMapper.toResponse(product);
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
    @Transactional
    public ProductResponse updateProduct(UUID id, ProductUpdateRequest request) {
        log.info("Updating product: {}", id);

        Product product = productRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        // Update sizes with proper CRUD logic
        if (request.getSizes() != null) {
            updateProductSizes(product, request.getSizes());
            // Flush to ensure size changes are persisted
            productSizeRepository.flush();
        }

        // Update images with smart MAIN logic
        if (request.getImages() != null) {
            updateProductImages(product, request.getImages());
            // Flush to ensure image changes are persisted
            productImageRepository.flush();
        }

        // Update product basic fields
        productMapper.updateEntity(request, product);
        Product updatedProduct = productRepository.save(product);
        
        // Flush all changes before returning
        productRepository.flush();

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
    // PROMOTION MANAGEMENT
    // ================================

    @Override
    public ProductPromotionResetResponse resetProductPromotion(UUID productId) {
        Product product = findProductByIdWithDetails(productId);
        User currentUser = securityUtils.getCurrentUser();
        
        validateUserBusinessAssociation(currentUser);
        validateBusinessOwnership(product, currentUser);
        
        boolean productHadPromotion = product.isPromotionActive();
        int sizesWithPromotions = resetProductSizes(product);
        
        product.removePromotion();
        productRepository.save(product);
        
        log.info("Reset all promotions for product {} - Product: {}, Sizes: {}", 
                productId, productHadPromotion, sizesWithPromotions);
        
        return promotionMapper.createProductResetResponse(
                product, currentUser.getBusinessId(), productHadPromotion, sizesWithPromotions);
    }

    @Override
    public SizePromotionResetResponse resetSizePromotion(UUID productId, UUID sizeId) {
        Product product = findProductByIdWithDetails(productId);
        User currentUser = securityUtils.getCurrentUser();
        
        validateUserBusinessAssociation(currentUser);
        validateBusinessOwnership(product, currentUser);
        
        ProductSize productSize = findSizeInProduct(product, sizeId);
        
        boolean hadPromotion = productSize.isPromotionActive();
        String originalPromotionType = productSize.getPromotionType() != null ? 
                productSize.getPromotionType().name() : null;
        
        productSize.removePromotion();
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
        
        List<ProductResponse> responses = productMapper.toResponseList(products);
        responses.forEach(response -> response.setIsFavorited(true));

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
    // SIZES CRUD OPERATIONS
    // ================================

    private void updateProductSizes(Product product, List<ProductSizeRequest> sizeRequests) {
        log.debug("Updating sizes for product {}: {} sizes provided", product.getId(), sizeRequests.size());

        if (sizeRequests.isEmpty()) {
            log.debug("Empty sizes list provided, deleting all existing sizes");
            productSizeRepository.deleteByProductIdAndIsDeletedFalse(product.getId());
            productSizeRepository.flush(); // Ensure deletion is completed
            return;
        }

        // Get current sizes fresh from database to avoid entity state issues
        List<ProductSize> currentSizes = product.getSizes() != null ? 
                new ArrayList<>(product.getSizes()) : new ArrayList<>();
        
        Set<UUID> requestedIds = sizeRequests.stream()
                .map(ProductSizeRequest::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // Delete sizes not in request
        List<UUID> sizesToDeleteIds = currentSizes.stream()
                .map(ProductSize::getId)
                .filter(id -> !requestedIds.contains(id))
                .toList();

        if (!sizesToDeleteIds.isEmpty()) {
            log.debug("Deleting {} sizes not in request", sizesToDeleteIds.size());
            // Delete by IDs to avoid entity state issues
            sizesToDeleteIds.forEach(id -> {
                productSizeRepository.findById(id).ifPresent(productSizeRepository::delete);
            });
            productSizeRepository.flush(); // Ensure deletions are completed
        }

        // Process each size request
        for (ProductSizeRequest sizeRequest : sizeRequests) {
            if (sizeRequest.getId() != null) {
                // Find existing size fresh from database
                Optional<ProductSize> existingSizeOpt = productSizeRepository.findById(sizeRequest.getId());

                if (existingSizeOpt.isPresent()) {
                    updateExistingSize(existingSizeOpt.get(), sizeRequest);
                } else {
                    createNewSize(product.getId(), sizeRequest);
                }
            } else {
                createNewSize(product.getId(), sizeRequest);
            }
        }
    }

    private void updateExistingSize(ProductSize existingSize, ProductSizeRequest request) {
        existingSize.setName(request.getName());
        existingSize.setPrice(request.getPrice());
        
        if (request.getPromotionType() != null) {
            try {
                existingSize.setPromotionType(PromotionType.valueOf(request.getPromotionType().toUpperCase()));
                existingSize.setPromotionValue(request.getPromotionValue());
                existingSize.setPromotionFromDate(request.getPromotionFromDate());
                existingSize.setPromotionToDate(request.getPromotionToDate());
            } catch (IllegalArgumentException e) {
                throw new ValidationException("Invalid promotion type: " + request.getPromotionType());
            }
        } else {
            existingSize.removePromotion();
        }
        
        productSizeRepository.save(existingSize);
    }

    private void createNewSize(UUID productId, ProductSizeRequest request) {
        ProductSize productSize = productSizeMapper.toEntity(request);
        productSize.setProductId(productId);

        if (request.getPromotionType() != null) {
            try {
                productSize.setPromotionType(PromotionType.valueOf(request.getPromotionType().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new ValidationException("Invalid promotion type: " + request.getPromotionType());
            }
        }

        productSizeRepository.save(productSize);
    }

    private void createProductSizes(UUID productId, List<ProductSizeRequest> sizeRequests) {
        if (sizeRequests == null || sizeRequests.isEmpty()) {
            return;
        }

        for (ProductSizeRequest sizeRequest : sizeRequests) {
            createNewSize(productId, sizeRequest);
        }
    }

    // ================================
    // IMAGES CRUD OPERATIONS WITH SMART MAIN LOGIC
    // ================================

    private void updateProductImages(Product product, List<ProductImageRequest> imageRequests) {
        log.debug("Updating images for product {}: {} images provided", product.getId(), imageRequests.size());

        if (imageRequests.isEmpty()) {
            productImageRepository.deleteByProductIdAndIsDeletedFalse(product.getId());
            productImageRepository.flush(); // Ensure deletion is completed
            return;
        }

        boolean isSingleImage = imageRequests.size() == 1;
        
        // Get current images fresh from database to avoid entity state issues
        List<ProductImage> currentImages = productImageRepository.findByProductIdOrderByMainAndSort(product.getId());
        
        Set<UUID> requestedIds = imageRequests.stream()
                .map(ProductImageRequest::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // Delete images not in request
        List<UUID> imagesToDeleteIds = currentImages.stream()
                .map(ProductImage::getId)
                .filter(id -> !requestedIds.contains(id))
                .toList();

        if (!imagesToDeleteIds.isEmpty()) {
            log.debug("Deleting {} images not in request", imagesToDeleteIds.size());
            // Delete by IDs to avoid entity state issues
            imagesToDeleteIds.forEach(id -> {
                productImageRepository.findById(id).ifPresent(productImageRepository::delete);
            });
            productImageRepository.flush(); // Ensure deletions are completed
        }

        boolean hasMainSet = false;

        // Process each image request
        for (ProductImageRequest imageRequest : imageRequests) {
            if (imageRequest.getId() != null) {
                // Find existing image fresh from database
                Optional<ProductImage> existingImageOpt = productImageRepository.findById(imageRequest.getId());
                
                if (existingImageOpt.isPresent()) {
                    hasMainSet = updateExistingImageSmart(existingImageOpt.get(), imageRequest, hasMainSet, isSingleImage) || hasMainSet;
                } else {
                    hasMainSet = createNewImageSmart(product.getId(), imageRequest, hasMainSet, isSingleImage) || hasMainSet;
                }
            } else {
                hasMainSet = createNewImageSmart(product.getId(), imageRequest, hasMainSet, isSingleImage) || hasMainSet;
            }
        }

        if (!isSingleImage && !hasMainSet) {
            ensureMainImageExists(product.getId());
        }
    }

    private boolean updateExistingImageSmart(ProductImage existingImage, ProductImageRequest request, 
                                           boolean hasMainAlreadySet, boolean isSingleImage) {
        existingImage.setImageUrl(request.getImageUrl());
        
        if (isSingleImage) {
            existingImage.setAsMain();
            productImageRepository.save(existingImage);
            return true;
        } else {
            if ("MAIN".equalsIgnoreCase(request.getImageType())) {
                if (!hasMainAlreadySet) {
                    existingImage.setAsMain();
                    productImageRepository.save(existingImage);
                    return true;
                } else {
                    existingImage.setAsGallery();
                }
            } else {
                existingImage.setAsGallery();
            }
            
            productImageRepository.save(existingImage);
            return false;
        }
    }

    private boolean createNewImageSmart(UUID productId, ProductImageRequest request, 
                                      boolean hasMainAlreadySet, boolean isSingleImage) {
        ProductImage productImage = productImageMapper.toEntity(request);
        productImage.setProductId(productId);

        if (isSingleImage) {
            productImage.setAsMain();
            productImageRepository.save(productImage);
            return true;
        } else {
            if ("MAIN".equalsIgnoreCase(request.getImageType())) {
                if (!hasMainAlreadySet) {
                    productImage.setAsMain();
                    productImageRepository.save(productImage);
                    return true;
                } else {
                    productImage.setAsGallery();
                }
            } else {
                productImage.setAsGallery();
            }
            
            productImageRepository.save(productImage);
            return false;
        }
    }

    private void createProductImages(UUID productId, List<ProductImageRequest> imageRequests) {
        if (imageRequests == null || imageRequests.isEmpty()) {
            return;
        }

        boolean isSingleImage = imageRequests.size() == 1;
        boolean hasMainImageSet = false;

        for (ProductImageRequest imageRequest : imageRequests) {
            ProductImage productImage = productImageMapper.toEntity(imageRequest);
            productImage.setProductId(productId);

            if (isSingleImage) {
                productImage.setAsMain();
                hasMainImageSet = true;
            } else {
                if ("MAIN".equalsIgnoreCase(imageRequest.getImageType())) {
                    if (!hasMainImageSet) {
                        productImage.setAsMain();
                        hasMainImageSet = true;
                    } else {
                        productImage.setAsGallery();
                    }
                } else {
                    productImage.setAsGallery();
                }
            }

            productImageRepository.save(productImage);
        }

        if (!isSingleImage && !hasMainImageSet) {
            setFirstImageAsMain(productId);
        }
    }

    private void ensureMainImageExists(UUID productId) {
        List<ProductImage> images = productImageRepository.findByProductIdOrderByMainAndSort(productId);
        
        if (images.isEmpty()) {
            return;
        }

        List<ProductImage> mainImages = images.stream()
                .filter(ProductImage::isMain)
                .toList();

        if (mainImages.isEmpty()) {
            setFirstImageAsMain(productId);
        } else if (mainImages.size() > 1) {
            fixMultipleMainImages(mainImages);
        }
    }

    private void setFirstImageAsMain(UUID productId) {
        List<ProductImage> images = productImageRepository.findByProductIdOrderByMainAndSort(productId);
        if (!images.isEmpty()) {
            ProductImage firstImage = images.get(0);
            firstImage.setAsMain();
            productImageRepository.save(firstImage);
        }
    }

    private void fixMultipleMainImages(List<ProductImage> mainImages) {
        if (mainImages.size() <= 1) {
            return;
        }

        for (int i = 1; i < mainImages.size(); i++) {
            ProductImage image = mainImages.get(i);
            image.setAsGallery();
            productImageRepository.save(image);
        }
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
        productRepository.findByIdWithImages(product.getId())
                .ifPresent(p -> product.setImages(p.getImages()));
        productRepository.findByIdWithSizes(product.getId())
                .ifPresent(p -> product.setSizes(p.getSizes()));
        return product;
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

    private ProductSize findSizeInProduct(Product product, UUID sizeId) {
        return product.getSizes().stream()
                .filter(size -> size.getId().equals(sizeId))
                .findFirst()
                .orElseThrow(() -> new ValidationException("Size not found in this product"));
    }

    private int resetProductSizes(Product product) {
        if (product.getSizes() == null || product.getSizes().isEmpty()) {
            return 0;
        }

        int sizesWithPromotions = 0;
        for (ProductSize size : product.getSizes()) {
            if (size.isPromotionActive()) {
                sizesWithPromotions++;
                size.removePromotion();
            }
        }
        
        if (sizesWithPromotions > 0) {
            productSizeRepository.saveAll(product.getSizes());
        }
        
        return sizesWithPromotions;
    }

    private Pageable createPageable(ProductFilterRequest filter) {
        int pageNo = filter.getPageNo() != null && filter.getPageNo() > 0 ? filter.getPageNo() - 1 : 0;
        return PaginationUtils.createPageable(
                pageNo, filter.getPageSize(), filter.getSortBy(), filter.getSortDirection()
        );
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
            products.forEach(product -> product.setIsFavorited(false));
        }
    }
}
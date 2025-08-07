// Fixed ProductServiceImpl.java - Key methods only
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

import java.util.List;
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
        
        // ✅ FIXED: Load collections separately for each product in the page
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

        if (request.getSizes() != null) {
            // Force initialization of sizes collection
            int existingSizesCount = product.getSizes() != null ? product.getSizes().size() : 0;
            log.debug("Product {} has {} existing sizes", id, existingSizesCount);

            // Delete existing sizes
            productSizeRepository.deleteByProductIdAndIsDeletedFalse(id);
            productSizeRepository.flush(); // Ensure deletion is completed

            // Create new sizes
            createProductSizes(id, request.getSizes());
        }

        if (request.getImages() != null) {
            // Force initialization of images collection
            int existingImagesCount = product.getImages() != null ? product.getImages().size() : 0;
            log.debug("Product {} has {} existing images", id, existingImagesCount);

            // Delete existing images
            productImageRepository.deleteByProductIdAndIsDeletedFalse(id);
            productImageRepository.flush(); // Ensure deletion is completed

            // Create new images
            createProductImages(id, request.getImages());
        }

        productMapper.updateEntity(request, product);
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

    // ✅ FIXED: Load product with collections separately to avoid MultipleBagFetchException
    private Product findProductByIdWithDetails(UUID id) {
        // First, load the product with basic relationships
        Product product = productRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        // Then load collections separately
        return loadProductCollections(product);
    }

    // ✅ ADDED: Helper method to load collections separately
    private Product loadProductCollections(Product product) {
        // Load images separately
        productRepository.findByIdWithImages(product.getId())
                .ifPresent(p -> product.setImages(p.getImages()));

        // Load sizes separately
        productRepository.findByIdWithSizes(product.getId())
                .ifPresent(p -> product.setSizes(p.getSizes()));

        return product;
    }

    // ================================
    // ENHANCED FAVORITE OPERATIONS (Using Mappers)
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
                .map(this::loadProductCollections) // ✅ FIXED: Load collections for each product
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
    // PRIVATE HELPER METHODS
    // ================================

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

    private void createProductSizes(UUID productId, List<ProductSizeRequest> sizeRequests) {
        if (sizeRequests == null || sizeRequests.isEmpty()) {
            return;
        }

        for (ProductSizeRequest sizeRequest : sizeRequests) {
            ProductSize productSize = productSizeMapper.toEntity(sizeRequest);
            productSize.setProductId(productId);

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
            products.forEach(product -> product.setIsFavorited(false));
        }
    }
}
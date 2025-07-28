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
import com.emenu.features.product.dto.response.ProductSummaryResponse;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

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

        // Check if product name already exists for this business
        if (productRepository.existsByNameAndBusinessIdAndIsDeletedFalse(
                request.getName(), currentUser.getBusinessId())) {
            throw new ValidationException("Product name already exists in your business");
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
    public PaginationResponse<ProductSummaryResponse> getAllProductsSummary(ProductFilterRequest filter) {
        
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
        return productMapper.toSummaryPaginationResponse(productPage);
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
        
        // Increment view count
        productRepository.incrementViewCount(id);
        
        ProductResponse response = productMapper.toResponse(product);
        
        // Set favorite status for current user (if logged in)
        try {
            setFavoriteStatusForUser(List.of(response));
        } catch (Exception e) {
            // User not logged in, keep isFavorited as false
            log.debug("User not logged in for product view: {}", id);
        }
        
        return response;
    }

    @Override
    public ProductResponse updateProduct(UUID id, ProductUpdateRequest request) {
        Product product = findProductByIdWithDetails(id);

        // Check if new name already exists (if name is being changed)
        if (request.getName() != null && !request.getName().equals(product.getName())) {
            if (productRepository.existsByNameAndBusinessIdAndIsDeletedFalse(
                    request.getName(), product.getBusinessId())) {
                throw new ValidationException("Product name already exists in your business");
            }
        }

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

    @Override
    public void incrementProductView(UUID id) {
        productRepository.incrementViewCount(id);
        log.debug("Incremented view count for product: {}", id);
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
        User currentUser = securityUtils.getCurrentUser();
        
        List<ProductFavorite> favorites = productFavoriteRepository.findFavoritesByUserId(currentUser.getId());
        List<Product> products = favorites.stream()
                .map(ProductFavorite::getProduct)
                .toList();

        List<ProductResponse> responses = productMapper.toResponseList(products);
        
        // Set all as favorited
        responses.forEach(response -> response.setIsFavorited(true));

        // Create pagination response manually
        return PaginationResponse.<ProductResponse>builder()
                .content(responses)
                .pageNo(1)
                .pageSize(responses.size())
                .totalElements(responses.size())
                .totalPages(1)
                .first(true)
                .last(true)
                .hasNext(false)
                .hasPrevious(false)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getProductsByCategory(UUID categoryId) {
        List<Product> products = productRepository.findByCategoryIdAndStatus(
                categoryId, com.emenu.enums.product.ProductStatus.ACTIVE);
        return productMapper.toResponseList(products);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getProductsByBrand(UUID brandId) {
        List<Product> products = productRepository.findByBrandId(brandId);
        return productMapper.toResponseList(products);
    }

    // Private helper methods
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
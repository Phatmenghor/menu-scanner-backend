package com.emenu.features.product.service.impl;

import com.emenu.exception.custom.NotFoundException;
import com.emenu.exception.custom.ValidationException;
import com.emenu.features.auth.models.User;
import com.emenu.features.product.dto.filter.ProductFilterDto;
import com.emenu.features.product.dto.request.ProductCreateDto;
import com.emenu.features.product.dto.request.ProductImageCreateDto;
import com.emenu.features.product.dto.request.ProductSizeCreateDto;
import com.emenu.features.product.dto.response.ProductDetailDto;
import com.emenu.features.product.dto.response.ProductListDto;
import com.emenu.features.product.mapper.ProductImageMapper;
import com.emenu.features.product.mapper.ProductMapper;
import com.emenu.features.product.mapper.ProductSizeMapper;
import com.emenu.features.product.models.Product;
import com.emenu.features.product.models.ProductImage;
import com.emenu.features.product.models.ProductSize;
import com.emenu.features.product.repository.ProductImageRepository;
import com.emenu.features.product.repository.ProductRepository;
import com.emenu.features.product.repository.ProductSizeRepository;
import com.emenu.features.product.service.ProductService;
import com.emenu.features.product.specification.ProductSpecifications;
import com.emenu.features.product.utils.ProductFavoriteQueryHelper;
import com.emenu.features.product.utils.ProductUtils;
import com.emenu.security.SecurityUtils;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.mapper.PaginationMapper;
import com.emenu.shared.pagination.PaginationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
    private final ProductMapper productMapper;
    private final ProductImageMapper productImageMapper;
    private final ProductSizeMapper productSizeMapper;
    private final PaginationMapper paginationMapper;
    private final SecurityUtils securityUtils;
    private final ProductUtils productUtils;
    private final ProductFavoriteQueryHelper favoriteQueryHelper;

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<ProductListDto> getAllProducts(ProductFilterDto filter) {
        log.info("Getting products with filter - Page: {}, Size: {}", filter.getPageNo(), filter.getPageSize());

        // Apply business filter for authenticated business users
        Optional<User> currentUser = securityUtils.getCurrentUserOptional();
        if (currentUser.isPresent() && currentUser.get().isBusinessUser() && filter.getBusinessId() == null) {
            filter.setBusinessId(currentUser.get().getBusinessId());
        }

        // Create pageable
        Pageable pageable = PaginationUtils.createPageable(
            filter.getPageNo() != null ? filter.getPageNo() - 1 : null,
            filter.getPageSize(),
            filter.getSortBy(),
            filter.getSortDirection()
        );
        
        // Build specification and execute query
        Specification<Product> spec = ProductSpecifications.withFilter(filter);
        Page<Product> productPage = productRepository.findAll(spec, pageable);
        
        // Map to DTOs
        PaginationResponse<ProductListDto> response = paginationMapper.toPaginationResponse(
            productPage,
                productMapper::toListDtos
        );
        
        // Enrich with favorite status if user is authenticated
        currentUser.ifPresent(user -> enrichProductsWithFavorites(response.getContent(), user.getId()));

        log.info("Retrieved {} products", response.getContent().size());
        return response;
    }

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
            boolean isFavorited = favoriteQueryHelper.isFavorited(currentUser.get().getId(), product.getId());
            dto.setIsFavorited(isFavorited);
        }

        return dto;
    }

    @Override
    @Transactional
    public ProductDetailDto getProductByIdPublic(UUID id) {
        log.info("Getting product by ID (public): {}", id);

        Product product = productRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new NotFoundException("Product not found with ID: " + id));

        if (!product.isActive()) {
            throw new NotFoundException("Product is not available");
        }

        // Increment view count
        productRepository.incrementViewCount(id);
        
        ProductDetailDto dto = productMapper.toDetailDto(product);
        
        // Enrich with favorite status if user is authenticated
        Optional<User> currentUser = securityUtils.getCurrentUserOptional();
        if (currentUser.isPresent()) {
            boolean isFavorited = favoriteQueryHelper.isFavorited(currentUser.get().getId(), product.getId());
            dto.setIsFavorited(isFavorited);
        }

        return dto;
    }

    @Override
    public ProductDetailDto createProduct(ProductCreateDto request) {
        log.info("Creating product: {}", request.getName());

        User currentUser = securityUtils.getCurrentUser();
        validateUserBusinessAssociation(currentUser);

        // Create main product entity
        Product product = productMapper.toEntity(request);
        product.setBusinessId(currentUser.getBusinessId());
        product.setViewCount(0L);
        product.setFavoriteCount(0L);
        
        // Save product first to get ID
        Product savedProduct = productRepository.save(product);

        // Handle images and sizes
        handleProductImages(savedProduct, request.getImages());
        handleProductSizes(savedProduct, request.getSizes());

        productUtils.logProductOperation("CREATE", savedProduct.getName(), currentUser.getBusinessId().toString());
        
        log.info("Product created successfully: {}", savedProduct.getName());
        return getProductById(savedProduct.getId());
    }

    @Override
    public ProductDetailDto updateProduct(UUID id, ProductCreateDto request) {
        log.info("Updating product: {}", id);

        Product product = productRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Product not found with ID: " + id));

        User currentUser = securityUtils.getCurrentUser();
        validateBusinessOwnership(product, currentUser);

        // Update basic product fields
        updateProductFromDto(product, request);
        Product updatedProduct = productRepository.save(product);
        
        // Handle collections
        updateProductImages(updatedProduct, request.getImages());
        updateProductSizes(updatedProduct, request.getSizes());

        productUtils.logProductOperation("UPDATE", updatedProduct.getName(), currentUser.getBusinessId().toString());
        
        log.info("Product updated successfully: {}", updatedProduct.getName());
        return getProductById(updatedProduct.getId());
    }

    @Override
    public ProductDetailDto deleteProduct(UUID id) {
        log.info("Deleting product: {}", id);

        Product product = productRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Product not found with ID: " + id));

        User currentUser = securityUtils.getCurrentUser();
        validateBusinessOwnership(product, currentUser);

        product.softDelete();
        Product deletedProduct = productRepository.save(product);

        productUtils.logProductOperation("DELETE", deletedProduct.getName(), currentUser.getBusinessId().toString());
        
        log.info("Product deleted successfully: {}", deletedProduct.getName());
        return productMapper.toDetailDto(deletedProduct);
    }

    // ================================
    // Helper Methods
    // ================================

    private void enrichProductsWithFavorites(List<ProductListDto> products, UUID userId) {
        if (userId == null || products.isEmpty()) {
            return;
        }

        List<UUID> productIds = products.stream()
                .map(ProductListDto::getId)
                .toList();

        // Batch query for favorites using helper
        List<UUID> favoriteProductIds = favoriteQueryHelper.getFavoriteProductIds(userId, productIds);
        
        products.forEach(product -> 
            product.setIsFavorited(favoriteProductIds.contains(product.getId())));
    }

    private void handleProductImages(Product product, List<ProductImageCreateDto> imageDtos) {
        if (imageDtos == null || imageDtos.isEmpty()) return;

        List<ProductImage> images = new ArrayList<>();
        boolean hasMainImage = false;

        for (int i = 0; i < imageDtos.size(); i++) {
            ProductImageCreateDto imageDto = imageDtos.get(i);
            
            if (!productUtils.isValidImageUrl(imageDto.getImageUrl())) {
                log.warn("Invalid image URL skipped: {}", imageDto.getImageUrl());
                continue;
            }

            ProductImage image = productImageMapper.toEntity(imageDto);
            image.setProductId(product.getId());
            
            // Set first image as main if no main image specified
            if (i == 0 && "GALLERY".equals(imageDto.getImageType())) {
                image.setImageType(com.emenu.enums.product.ImageType.MAIN);
                hasMainImage = true;
            }
            
            images.add(image);
        }

        if (!images.isEmpty()) {
            productImageRepository.saveAll(images);
        }
    }

    private void handleProductSizes(Product product, List<ProductSizeCreateDto> sizeDtos) {
        if (sizeDtos == null || sizeDtos.isEmpty()) return;

        List<ProductSize> sizes = sizeDtos.stream()
                .map(sizeDto -> {
                    ProductSize size = productSizeMapper.toEntity(sizeDto);
                    size.setProductId(product.getId());
                    return size;
                })
                .collect(Collectors.toList());

        productSizeRepository.saveAll(sizes);
    }

    private void updateProductImages(Product product, List<ProductImageCreateDto> imageDtos) {
        // Delete existing images
        List<ProductImage> existingImages = productImageRepository.findByProductIdOrderByMainAndSort(product.getId());
        if (!existingImages.isEmpty()) {
            productImageRepository.deleteAll(existingImages);
        }
        
        handleProductImages(product, imageDtos);
    }

    private void updateProductSizes(Product product, List<ProductSizeCreateDto> sizeDtos) {
        // Delete existing sizes
        List<ProductSize> existingSizes = product.getSizes();
        if (existingSizes != null && !existingSizes.isEmpty()) {
            productSizeRepository.deleteAll(existingSizes);
        }
        
        handleProductSizes(product, sizeDtos);
    }

    private void updateProductFromDto(Product product, ProductCreateDto dto) {
        product.setName(productUtils.sanitizeProductName(dto.getName()));
        product.setDescription(dto.getDescription());
        product.setCategoryId(dto.getCategoryId());
        product.setBrandId(dto.getBrandId());
        product.setPrice(dto.getPrice());
        product.setStatus(dto.getStatus());
        
        // Update promotion fields
        if (StringUtils.hasText(dto.getPromotionType()) && dto.getPromotionValue() != null) {
            product.setPromotionType(com.emenu.enums.product.PromotionType.valueOf(dto.getPromotionType().toUpperCase()));
            product.setPromotionValue(dto.getPromotionValue());
            product.setPromotionFromDate(dto.getPromotionFromDate());
            product.setPromotionToDate(dto.getPromotionToDate());
        } else {
            clearProductPromotion(product);
        }
    }

    private void clearProductPromotion(Product product) {
        product.setPromotionType(null);
        product.setPromotionValue(null);
        product.setPromotionFromDate(null);
        product.setPromotionToDate(null);
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
        if (user.isBusinessUser() && !product.getBusinessId().equals(user.getBusinessId())) {
            throw new ValidationException("Access denied to product from different business");
        }
    }
}
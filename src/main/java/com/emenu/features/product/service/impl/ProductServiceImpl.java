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
import com.emenu.features.product.dto.update.ProductImageUpdateDto;
import com.emenu.features.product.dto.update.ProductSizeUpdateDto;
import com.emenu.features.product.dto.update.ProductUpdateDto;
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

import java.util.*;
import java.util.concurrent.CompletableFuture;

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
        log.info("Getting products - Page: {}, Size: {}", filter.getPageNo(), filter.getPageSize());

        // Set business filter for business users
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

        // Use specifications for flexible filtering
        Specification<Product> spec = ProductSpecifications.withFilter(filter);
        Page<Product> productPage = productRepository.findAll(spec, pageable);

        if (productPage.getContent().isEmpty()) {
            return paginationMapper.toPaginationResponse(productPage, Collections.emptyList());
        }

        // Process products with async operations
        List<ProductListDto> dtoList = processProducts(productPage.getContent(), currentUser.orElse(null));
        return paginationMapper.toPaginationResponse(productPage, dtoList);
    }

    /**
     * Process products with async batch loading
     */
    private List<ProductListDto> processProducts(List<Product> products, User currentUser) {
        List<UUID> productIds = products.stream().map(Product::getId).toList();
        
        // Start async operations
        CompletableFuture<Map<UUID, List<ProductSize>>> sizesFuture = CompletableFuture.supplyAsync(() -> 
            productSizeRepository.findSizesByProductIdsGrouped(productIds));
        
        CompletableFuture<List<UUID>> favoritesFuture = CompletableFuture.supplyAsync(() -> 
            currentUser != null ? favoriteQueryHelper.getFavoriteProductIds(currentUser.getId(), productIds) : Collections.emptyList());

        // Convert to DTOs
        List<ProductListDto> dtoList = productMapper.toListDtos(products);

        try {
            // Get async results
            Map<UUID, List<ProductSize>> sizesMap = sizesFuture.get();
            List<UUID> favoriteIds = favoritesFuture.get();
            Set<UUID> favoriteSet = new HashSet<>(favoriteIds);

            // Enrich DTOs
            for (int i = 0; i < dtoList.size(); i++) {
                ProductListDto dto = dtoList.get(i);
                Product product = products.get(i);
                
                List<ProductSize> sizes = sizesMap.getOrDefault(dto.getId(), Collections.emptyList());
                dto.setHasSizes(!sizes.isEmpty());
                
                setDisplayFields(dto, product, sizes);
                dto.setIsFavorited(favoriteSet.contains(dto.getId()));
            }

        } catch (Exception e) {
            log.error("Error processing products", e);
            dtoList.forEach(dto -> {
                dto.setHasSizes(false);
                dto.setIsFavorited(false);
            });
        }

        return dtoList;
    }

    /**
     * Set display fields for product
     */
    private void setDisplayFields(ProductListDto dto, Product product, List<ProductSize> sizes) {
        if (sizes.isEmpty()) {
            // Use product data
            dto.setDisplayOriginPrice(product.getPrice());
            dto.setDisplayPromotionType(product.getPromotionType() != null ? product.getPromotionType().name() : null);
            dto.setDisplayPromotionValue(product.getPromotionValue());
            dto.setDisplayPromotionFromDate(product.getPromotionFromDate());
            dto.setDisplayPromotionToDate(product.getPromotionToDate());
            dto.setHasPromotion(product.isPromotionActive());
        } else {
            // Use first size (smallest price)
            ProductSize firstSize = sizes.get(0);
            
            dto.setDisplayOriginPrice(firstSize.getPrice());
            dto.setDisplayPromotionType(firstSize.getPromotionType() != null ? firstSize.getPromotionType().name() : null);
            dto.setDisplayPromotionValue(firstSize.getPromotionValue());
            dto.setDisplayPromotionFromDate(firstSize.getPromotionFromDate());
            dto.setDisplayPromotionToDate(firstSize.getPromotionToDate());
            dto.setHasPromotion(firstSize.isPromotionActive());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDetailDto getProductById(UUID id) {
        log.info("Getting product details: {}", id);

        Product product = productRepository.findByIdWithAllDetails(id)
                .orElseThrow(() -> new NotFoundException("Product not found with ID: " + id));

        Optional<User> currentUser = securityUtils.getCurrentUserOptional();
        if (currentUser.isPresent() && currentUser.get().isBusinessUser()) {
            validateBusinessAccess(product, currentUser.get());
        }

        ProductDetailDto dto = productMapper.toDetailDto(product);

        if (currentUser.isPresent()) {
            boolean isFavorited = favoriteQueryHelper.isFavorited(currentUser.get().getId(), product.getId());
            dto.setIsFavorited(isFavorited);
        }

        return dto;
    }

    @Override
    @Transactional
    public ProductDetailDto getProductByIdPublic(UUID id) {
        log.info("Getting public product: {}", id);

        Product product = productRepository.findByIdWithAllDetails(id)
                .orElseThrow(() -> new NotFoundException("Product not found with ID: " + id));

        // Async view count increment
        CompletableFuture.runAsync(() -> {
            try {
                productRepository.incrementViewCount(id);
            } catch (Exception e) {
                log.warn("Failed to increment view count for product {}: {}", id, e.getMessage());
            }
        });

        ProductDetailDto dto = productMapper.toDetailDto(product);

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

        Product product = productMapper.toEntity(request);
        product.setBusinessId(currentUser.getBusinessId());
        product.setViewCount(0L);
        product.setFavoriteCount(0L);

        Product savedProduct = productRepository.save(product);

        handleProductImages(savedProduct, request.getImages());
        handleProductSizes(savedProduct, request.getSizes());

        log.info("Product created: {}", savedProduct.getName());
        return getProductById(savedProduct.getId());
    }

    @Override
    public ProductDetailDto updateProduct(UUID id, ProductUpdateDto request) {
        log.info("Updating product: {}", id);

        Product product = productRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Product not found with ID: " + id));

        User currentUser = securityUtils.getCurrentUser();
        validateBusinessOwnership(product, currentUser);

        productMapper.updateEntityFromDto(request, product);
        Product updatedProduct = productRepository.save(product);

        updateProductImages(updatedProduct, request.getImages());
        updateProductSizes(updatedProduct, request.getSizes());

        log.info("Product updated: {}", updatedProduct.getName());
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

        log.info("Product deleted: {}", deletedProduct.getName());
        return productMapper.toDetailDto(deletedProduct);
    }

    // Helper methods
    private void handleProductImages(Product product, List<ProductImageCreateDto> imageDtos) {
        if (imageDtos == null || imageDtos.isEmpty()) return;

        List<ProductImage> images = imageDtos.stream()
                .filter(imageDto -> productUtils.isValidImageUrl(imageDto.getImageUrl()))
                .map(imageDto -> {
                    ProductImage image = productImageMapper.toEntity(imageDto);
                    image.setProductId(product.getId());
                    return image;
                })
                .toList();

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
                .toList();

        productSizeRepository.saveAll(sizes);
    }

    private void updateProductImages(Product product, List<ProductImageUpdateDto> imageDtos) {
        if (imageDtos == null || imageDtos.isEmpty()) return;

        List<ProductImage> existingImages = productImageRepository.findByProductIdOrderByMainAndSort(product.getId());

        List<UUID> idsToDelete = productImageMapper.getIdsToDelete(imageDtos);
        if (!idsToDelete.isEmpty()) {
            existingImages.stream()
                    .filter(img -> idsToDelete.contains(img.getId()))
                    .forEach(img -> {
                        img.softDelete();
                        productImageRepository.save(img);
                    });
        }

        List<ProductImageUpdateDto> toUpdate = productImageMapper.getExistingToUpdate(imageDtos);
        for (ProductImageUpdateDto updateDto : toUpdate) {
            existingImages.stream()
                    .filter(img -> img.getId().equals(updateDto.getId()))
                    .findFirst()
                    .ifPresent(existingImage -> {
                        productImageMapper.updateEntityFromDto(updateDto, existingImage);
                        productImageRepository.save(existingImage);
                    });
        }

        List<ProductImage> newImages = productImageMapper.toEntitiesFromUpdate(imageDtos);
        newImages.forEach(img -> img.setProductId(product.getId()));
        if (!newImages.isEmpty()) {
            productImageRepository.saveAll(newImages);
        }
    }

    private void updateProductSizes(Product product, List<ProductSizeUpdateDto> sizeDtos) {
        if (sizeDtos == null || sizeDtos.isEmpty()) return;

        List<ProductSize> existingSizes = productSizeRepository.findByProductIdAndIsDeletedFalse(product.getId());

        List<UUID> idsToDelete = productSizeMapper.getIdsToDelete(sizeDtos);
        if (!idsToDelete.isEmpty()) {
            existingSizes.stream()
                    .filter(size -> idsToDelete.contains(size.getId()))
                    .forEach(size -> {
                        size.softDelete();
                        productSizeRepository.save(size);
                    });
        }

        List<ProductSizeUpdateDto> toUpdate = productSizeMapper.getExistingToUpdate(sizeDtos);
        for (ProductSizeUpdateDto updateDto : toUpdate) {
            existingSizes.stream()
                    .filter(size -> size.getId().equals(updateDto.getId()))
                    .findFirst()
                    .ifPresent(existingSize -> {
                        productSizeMapper.updateEntityFromDto(updateDto, existingSize);
                        productSizeRepository.save(existingSize);
                    });
        }

        List<ProductSize> newSizes = productSizeMapper.toEntitiesFromUpdate(sizeDtos);
        newSizes.forEach(size -> size.setProductId(product.getId()));
        if (!newSizes.isEmpty()) {
            productSizeRepository.saveAll(newSizes);
        }
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
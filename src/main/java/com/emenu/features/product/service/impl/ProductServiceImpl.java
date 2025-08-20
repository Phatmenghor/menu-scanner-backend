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
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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
        log.info("🚀 High-performance product search - Page: {}, Size: {}", filter.getPageNo(), filter.getPageSize());

        Optional<User> currentUser = securityUtils.getCurrentUserOptional();
        if (currentUser.isPresent() && currentUser.get().isBusinessUser() && filter.getBusinessId() == null) {
            filter.setBusinessId(currentUser.get().getBusinessId());
        }

        Pageable pageable = PaginationUtils.createPageable(
                filter.getPageNo() != null ? filter.getPageNo() - 1 : null,
                filter.getPageSize(),
                filter.getSortBy(),
                filter.getSortDirection()
        );

        Specification<Product> spec = ProductSpecifications.withFilter(filter);
        Page<Product> productPage = productRepository.findAll(spec, pageable);

        if (!productPage.getContent().isEmpty()) {
            // ✅ OPTIMIZED: Batch load relationships
            List<UUID> productIds = productPage.getContent().stream()
                    .map(Product::getId)
                    .toList();

            var productsWithRelationships = productRepository.findByIdInWithRelationships(productIds);

            // ✅ Map back to maintain order
            Map<UUID, Product> productMap = productsWithRelationships.stream()
                    .collect(java.util.stream.Collectors.toMap(Product::getId, p -> p));

            productPage.getContent().forEach(product -> {
                Product enriched = productMap.get(product.getId());
                if (enriched != null) {
                    product.setBusiness(enriched.getBusiness());
                    product.setCategory(enriched.getCategory());
                    product.setBrand(enriched.getBrand());
                }
            });
        }

        if (!productPage.getContent().isEmpty()) {
            batchLoadSizes(productPage.getContent());
        }

        PaginationResponse<ProductListDto> response = paginationMapper.toPaginationResponse(
                productPage,
                productMapper::toListDtos
        );

        currentUser.ifPresent(user -> batchEnrichFavorites(response.getContent(), user.getId()));
        return response;
    }

    /**
     * 🚀 OPTIMIZED: Try direct optimized queries based on filter
     */
    private Page<Product> tryOptimizedQuery(ProductFilterDto filter, Pageable pageable) {
        // Simple business filter
        if (filter.getBusinessId() != null &&
                filter.getCategoryId() == null &&
                filter.getBrandId() == null &&
                !StringUtils.hasText(filter.getSearch()) &&
                filter.getHasPromotion() == null) {

            log.debug("🎯 Using optimized business query");
            return productRepository.findByBusinessIdWithRelationships(filter.getBusinessId(), pageable);
        }

        // Simple category filter
        if (filter.getCategoryId() != null &&
                filter.getBusinessId() == null &&
                filter.getBrandId() == null &&
                !StringUtils.hasText(filter.getSearch()) &&
                filter.getHasPromotion() == null) {

            log.debug("🎯 Using optimized category query");
            return productRepository.findByCategoryIdWithRelationships(filter.getCategoryId(), pageable);
        }

        // Simple search
        if (StringUtils.hasText(filter.getSearch()) &&
                filter.getBusinessId() == null &&
                filter.getCategoryId() == null &&
                filter.getBrandId() == null &&
                filter.getHasPromotion() == null) {

            log.debug("🎯 Using optimized search query");
            String searchPattern = "%" + filter.getSearch() + "%";
            return productRepository.findBySearchWithRelationships(searchPattern, pageable);
        }

        // No optimization available
        return null;
    }

    /**
     * 🚀 OPTIMIZED: Batch load sizes for multiple products
     */
    private void batchLoadSizes(List<Product> products) {
        if (products.isEmpty()) return;

        List<UUID> productIds = products.stream()
                .map(Product::getId)
                .toList();

        // ✅ Single query to get all sizes
        Map<UUID, List<ProductSize>> sizesByProduct = productSizeRepository.findSizesByProductIdsGrouped(productIds);

        // ✅ Assign sizes to products
        products.forEach(product -> {
            List<ProductSize> sizes = sizesByProduct.getOrDefault(product.getId(), List.of());
            product.getSizes().clear();
            product.getSizes().addAll(sizes);
        });
    }

    /**
     * 🚀 OPTIMIZED: Batch enrich favorites
     */
    private void batchEnrichFavorites(List<ProductListDto> products, UUID userId) {
        if (products.isEmpty()) return;

        List<UUID> productIds = products.stream()
                .map(ProductListDto::getId)
                .toList();

        List<UUID> favoriteProductIds = favoriteQueryHelper.getFavoriteProductIds(userId, productIds);

        products.forEach(product ->
                product.setIsFavorited(favoriteProductIds.contains(product.getId())));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDetailDto getProductById(UUID id) {
        log.info("🔍 Getting product details: {}", id);

        // ✅ OPTIMIZED: Single query with all details including sizes
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
        log.info("🌐 Getting public product: {}", id);

        Product product = productRepository.findByIdWithAllDetails(id)
                .orElseThrow(() -> new NotFoundException("Product not found with ID: " + id));

        // ✅ Async increment view count (non-blocking)
        productRepository.incrementViewCount(id);

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
        log.info("📝 Creating product: {}", request.getName());

        User currentUser = securityUtils.getCurrentUser();
        validateUserBusinessAssociation(currentUser);

        Product product = productMapper.toEntity(request);
        product.setBusinessId(currentUser.getBusinessId());
        product.setViewCount(0L);
        product.setFavoriteCount(0L);

        Product savedProduct = productRepository.save(product);

        handleProductImages(savedProduct, request.getImages());
        handleProductSizes(savedProduct, request.getSizes());

        log.info("✅ Product created: {}", savedProduct.getName());
        return getProductById(savedProduct.getId());
    }

    @Override
    public ProductDetailDto updateProduct(UUID id, ProductUpdateDto request) {
        log.info("✏️ Updating product: {}", id);

        Product product = productRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Product not found with ID: " + id));

        User currentUser = securityUtils.getCurrentUser();
        validateBusinessOwnership(product, currentUser);

        productMapper.updateEntityFromDto(request, product);
        Product updatedProduct = productRepository.save(product);

        updateProductImages(updatedProduct, request.getImages());
        updateProductSizes(updatedProduct, request.getSizes());

        log.info("✅ Product updated: {}", updatedProduct.getName());
        return getProductById(updatedProduct.getId());
    }

    @Override
    public ProductDetailDto deleteProduct(UUID id) {
        log.info("🗑️ Deleting product: {}", id);

        Product product = productRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Product not found with ID: " + id));

        User currentUser = securityUtils.getCurrentUser();
        validateBusinessOwnership(product, currentUser);

        product.softDelete();
        Product deletedProduct = productRepository.save(product);

        log.info("✅ Product deleted: {}", deletedProduct.getName());
        return productMapper.toDetailDto(deletedProduct);
    }

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
            log.debug("Created {} images for product {}", images.size(), product.getId());
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
        log.debug("📏 Created {} sizes for product {}", sizes.size(), product.getId());
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
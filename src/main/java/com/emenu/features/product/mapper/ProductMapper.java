package com.emenu.features.product.mapper;

import com.emenu.enums.product.PromotionType;
import com.emenu.features.product.dto.request.ProductCreateRequest;
import com.emenu.features.product.dto.response.ProductImageResponse;
import com.emenu.features.product.dto.response.ProductResponse;
import com.emenu.features.product.dto.response.ProductSizeResponse;
import com.emenu.features.product.dto.update.ProductUpdateRequest;
import com.emenu.features.product.models.Product;
import com.emenu.features.product.models.ProductImage;
import com.emenu.features.product.models.ProductSize;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.mapper.PaginationMapper;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {ProductSizeMapper.class, ProductImageMapper.class})
public abstract class ProductMapper {

    @Autowired
    protected PaginationMapper paginationMapper;

    // ================================
    // ENTITY CREATION MAPPING
    // ================================

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "businessId", ignore = true) // Will be set from current user
    @Mapping(target = "business", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "brand", ignore = true)
    @Mapping(target = "images", ignore = true) // Will be handled separately
    @Mapping(target = "sizes", ignore = true) // Will be handled separately
    @Mapping(target = "viewCount", constant = "0L")
    @Mapping(target = "favoriteCount", constant = "0L")
    @Mapping(source = "promotionType", target = "promotionType", qualifiedByName = "mapStringToPromotionType")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    public abstract Product toEntity(ProductCreateRequest request);

    // ================================
    // RESPONSE MAPPING WITH BUSINESS LOGIC
    // ================================

    @Mapping(source = "business.name", target = "businessName")
    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(source = "brand.name", target = "brandName")
    @Mapping(source = "promotionType", target = "promotionType", qualifiedByName = "mapPromotionTypeToString")
    @Mapping(target = "mainImageUrl", expression = "java(extractMainImageUrl(product))")
    @Mapping(target = "displayPrice", expression = "java(calculateDisplayPrice(product))")
    @Mapping(target = "hasPromotionActive", expression = "java(hasActivePromotion(product))")
    @Mapping(target = "hasSizes", expression = "java(hasSizes(product))")
    @Mapping(target = "publicUrl", expression = "java(generatePublicUrl(product))")
    @Mapping(target = "isFavorited", constant = "false") // Will be overridden in service
    @Mapping(target = "images", expression = "java(sortImages(product.getImages()))")
    @Mapping(target = "sizes", expression = "java(sortSizes(product.getSizes()))")
    public abstract ProductResponse toResponse(Product product);

    public abstract List<ProductResponse> toResponseList(List<Product> products);

    // ================================
    // UPDATE MAPPING
    // ================================

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "businessId", ignore = true)
    @Mapping(target = "business", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "brand", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "sizes", ignore = true)
    @Mapping(target = "viewCount", ignore = true)
    @Mapping(target = "favoriteCount", ignore = true)
    @Mapping(source = "promotionType", target = "promotionType", qualifiedByName = "mapStringToPromotionType")
    public abstract void updateEntity(ProductUpdateRequest request, @MappingTarget Product product);

    // ================================
    // BUSINESS LOGIC METHODS
    // ================================

    /**
     * Extract main image URL with fallback logic
     */
    protected String extractMainImageUrl(Product product) {
        if (product.getImages() == null || product.getImages().isEmpty()) {
            return null;
        }

        // First try to find MAIN image
        return product.getImages().stream()
                .filter(img -> img.getImageType().name().equals("MAIN"))
                .findFirst()
                .map(ProductImage::getImageUrl)
                .orElse(product.getImages().get(0).getImageUrl()); // Fallback to first image
    }

    /**
     * Calculate display price considering sizes and promotions
     */
    protected BigDecimal calculateDisplayPrice(Product product) {
        if (hasSizes(product)) {
            // Get the lowest price from sizes (with active promotions considered)
            return product.getSizes().stream()
                    .map(this::calculateSizeFinalPrice)
                    .min(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);
        } else {
            // Use product-level price with promotion
            return calculateProductFinalPrice(product);
        }
    }

    /**
     * Check if product has active promotions
     */
    protected Boolean hasActivePromotion(Product product) {
        if (hasSizes(product)) {
            // Check if any size has active promotion
            return product.getSizes().stream()
                    .anyMatch(this::isSizePromotionActive);
        } else {
            // Check product-level promotion
            return isProductPromotionActive(product);
        }
    }

    /**
     * Check if product has sizes
     */
    protected Boolean hasSizes(Product product) {
        return product.getSizes() != null && !product.getSizes().isEmpty();
    }

    /**
     * Generate public URL for product
     */
    protected String generatePublicUrl(Product product) {
        return "/products/" + product.getId();
    }

    /**
     * Sort images: MAIN first, then GALLERY by creation date
     */
    protected List<com.emenu.features.product.dto.response.ProductImageResponse> sortImages(
            List<com.emenu.features.product.models.ProductImage> images) {
        if (images == null || images.isEmpty()) {
            return List.of();
        }

        return images.stream()
                .sorted((img1, img2) -> {
                    // MAIN images first
                    if (img1.getImageType().name().equals("MAIN") && !img2.getImageType().name().equals("MAIN")) {
                        return -1;
                    }
                    if (!img1.getImageType().name().equals("MAIN") && img2.getImageType().name().equals("MAIN")) {
                        return 1;
                    }
                    // Then sort by creation date (newest first)
                    return img2.getCreatedAt().compareTo(img1.getCreatedAt());
                })
                .map(this::mapImageToResponse)
                .toList();
    }

    /**
     * Sort sizes by price (lowest to highest)
     */
    protected List<com.emenu.features.product.dto.response.ProductSizeResponse> sortSizes(
            List<com.emenu.features.product.models.ProductSize> sizes) {
        if (sizes == null || sizes.isEmpty()) {
            return List.of();
        }

        return sizes.stream()
                .sorted((size1, size2) -> {
                    BigDecimal price1 = calculateSizeFinalPrice(size1);
                    BigDecimal price2 = calculateSizeFinalPrice(size2);
                    return price1.compareTo(price2); // Ascending order (lowest first)
                })
                .map(this::mapSizeToResponse)
                .toList();
    }

    // ================================
    // HELPER CALCULATION METHODS
    // ================================

    /**
     * Calculate final price for a product size with promotion
     */
    private BigDecimal calculateSizeFinalPrice(com.emenu.features.product.models.ProductSize size) {
        if (!isSizePromotionActive(size)) {
            return size.getPrice();
        }

        BigDecimal basePrice = size.getPrice();
        PromotionType promotionType = size.getPromotionType();
        BigDecimal promotionValue = size.getPromotionValue();

        if (promotionType == null || promotionValue == null) {
            return basePrice;
        }

        return switch (promotionType) {
            case PERCENTAGE -> {
                BigDecimal discount = basePrice.multiply(promotionValue)
                        .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
                yield basePrice.subtract(discount);
            }
            case FIXED_AMOUNT -> {
                BigDecimal finalPrice = basePrice.subtract(promotionValue);
                yield finalPrice.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : finalPrice;
            }
        };
    }

    /**
     * Calculate final price for product with promotion
     */
    private BigDecimal calculateProductFinalPrice(Product product) {
        if (!isProductPromotionActive(product)) {
            return product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO;
        }

        BigDecimal basePrice = product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO;
        PromotionType promotionType = product.getPromotionType();
        BigDecimal promotionValue = product.getPromotionValue();

        if (promotionType == null || promotionValue == null) {
            return basePrice;
        }

        return switch (promotionType) {
            case PERCENTAGE -> {
                BigDecimal discount = basePrice.multiply(promotionValue)
                        .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
                yield basePrice.subtract(discount);
            }
            case FIXED_AMOUNT -> {
                BigDecimal finalPrice = basePrice.subtract(promotionValue);
                yield finalPrice.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : finalPrice;
            }
        };
    }

    /**
     * Check if size promotion is active
     */
    private boolean isSizePromotionActive(com.emenu.features.product.models.ProductSize size) {
        if (size.getPromotionValue() == null || size.getPromotionType() == null) {
            return false;
        }

        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        if (size.getPromotionFromDate() != null && now.isBefore(size.getPromotionFromDate())) {
            return false;
        }

        if (size.getPromotionToDate() != null && now.isAfter(size.getPromotionToDate())) {
            return false;
        }

        return true;
    }

    /**
     * Check if product promotion is active
     */
    private boolean isProductPromotionActive(Product product) {
        if (product.getPromotionValue() == null || product.getPromotionType() == null) {
            return false;
        }

        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        if (product.getPromotionFromDate() != null && now.isBefore(product.getPromotionFromDate())) {
            return false;
        }

        if (product.getPromotionToDate() != null && now.isAfter(product.getPromotionToDate())) {
            return false;
        }

        return true;
    }

    // ================================
    // MAPPING HELPER METHODS
    // ================================

    /**
     * Map ProductImage entity to response
     */
    private ProductImageResponse mapImageToResponse(
            ProductImage image) {
        ProductImageResponse response = new ProductImageResponse();
        response.setId(image.getId());
        response.setImageUrl(image.getImageUrl());
        response.setImageType(image.getImageType().name());
        return response;
    }

    /**
     * Map ProductSize entity to response with calculated fields
     */
    private ProductSizeResponse mapSizeToResponse(ProductSize size) {
        ProductSizeResponse response = new ProductSizeResponse();
        response.setId(size.getId());
        response.setName(size.getName());
        response.setPrice(size.getPrice());
        response.setPromotionType(size.getPromotionType() != null ? size.getPromotionType().name() : null);
        response.setPromotionValue(size.getPromotionValue());
        response.setPromotionFromDate(size.getPromotionFromDate());
        response.setPromotionToDate(size.getPromotionToDate());
        response.setFinalPrice(calculateSizeFinalPrice(size));
        response.setIsPromotionActive(isSizePromotionActive(size));
        return response;
    }

    // ================================
    // PAGINATION MAPPING
    // ================================

    /**
     * Convert Page to PaginationResponse with enhanced sorting
     */
    public PaginationResponse<ProductResponse> toPaginationResponse(Page<Product> productPage) {
        List<ProductResponse> responses = productPage.getContent().stream()
                .map(this::toResponse)
                .toList();

        return paginationMapper.toPaginationResponse(productPage, responses);
    }

    // ================================
    // MAPSTRUCT QUALIFIERS
    // ================================

    @Named("mapStringToPromotionType")
    protected PromotionType mapStringToPromotionType(String promotionType) {
        if (promotionType == null || promotionType.trim().isEmpty()) return null;
        try {
            return PromotionType.valueOf(promotionType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Named("mapPromotionTypeToString")
    protected String mapPromotionTypeToString(PromotionType promotionType) {
        return promotionType != null ? promotionType.name() : null;
    }

    // ================================
    // UTILITY METHODS FOR SERVICE
    // ================================

    /**
     * Enrich product responses with user-specific data
     */
    public List<ProductResponse> enrichWithUserData(List<ProductResponse> responses, Function<UUID, Boolean> favoriteChecker) {
        return responses.stream()
                .peek(response -> {
                    try {
                        response.setIsFavorited(favoriteChecker.apply(response.getId()));
                    } catch (Exception e) {
                        response.setIsFavorited(false);
                    }
                })
                .toList();
    }

    /**
     * Enrich single product response with user-specific data
     */
    public ProductResponse enrichWithUserData(ProductResponse response,
                                              Function<UUID, Boolean> favoriteChecker) {
        try {
            response.setIsFavorited(favoriteChecker.apply(response.getId()));
        } catch (Exception e) {
            response.setIsFavorited(false);
        }
        return response;
    }
}
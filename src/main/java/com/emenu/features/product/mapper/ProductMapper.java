package com.emenu.features.product.mapper;

import com.emenu.enums.product.ProductStatus;
import com.emenu.enums.product.PromotionType;
import com.emenu.features.auth.models.User;
import com.emenu.features.product.dto.filter.ProductFilterRequest;
import com.emenu.features.product.dto.request.ProductCreateRequest;
import com.emenu.features.product.dto.response.ProductImageResponse;
import com.emenu.features.product.dto.response.ProductResponse;
import com.emenu.features.product.dto.response.ProductSizeResponse;
import com.emenu.features.product.dto.update.ProductUpdateRequest;
import com.emenu.features.product.models.Product;
import com.emenu.features.product.models.ProductFavorite;
import com.emenu.features.product.models.ProductImage;
import com.emenu.features.product.models.ProductSize;
import com.emenu.features.product.repository.ProductFavoriteRepository;
import com.emenu.features.product.repository.ProductRepository;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.mapper.PaginationMapper;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {ProductSizeMapper.class, ProductImageMapper.class})
@Slf4j
public abstract class ProductMapper {

    @Autowired
    protected PaginationMapper paginationMapper;
    @Autowired
    protected ProductRepository productRepository;
    @Autowired
    protected ProductFavoriteRepository productFavoriteRepository;

    // ================================
    // ENTITY CREATION MAPPING
    // ================================

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "businessId", ignore = true)
    @Mapping(target = "business", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "brand", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "sizes", ignore = true)
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
    // FAST LISTING METHODS - NATIVE QUERY SUPPORT
    // ================================

    /**
     * Fast pagination method using native queries for listings
     */
    public PaginationResponse<ProductResponse> getProductsWithNativeQuery(
            ProductFilterRequest filter, 
            Optional<User> currentUser) {
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Apply business filter for authenticated business users
            if (currentUser.isPresent() && currentUser.get().isBusinessUser() && filter.getBusinessId() == null) {
                filter.setBusinessId(currentUser.get().getBusinessId());
            }

            // Calculate pagination
            int pageNo = filter.getPageNo() != null && filter.getPageNo() > 0 ? filter.getPageNo() - 1 : 0;
            int pageSize = filter.getPageSize() != null ? filter.getPageSize() : 10;
            int offset = pageNo * pageSize;

            // Convert enum to string for native query
            String statusStr = filter.getStatus() != null ? filter.getStatus().name() : null;

            // Execute optimized native queries
            List<Object[]> rows = productRepository.findProductsForListing(
                    filter.getBusinessId(),
                    filter.getCategoryId(),
                    filter.getBrandId(),
                    statusStr,
                    filter.getSearch(),
                    pageSize,
                    offset
            );

            long totalElements = productRepository.countProductsForListing(
                    filter.getBusinessId(),
                    filter.getCategoryId(),
                    filter.getBrandId(),
                    statusStr,
                    filter.getSearch()
            );

            // Convert native query results to DTOs
            List<ProductResponse> content = mapNativeQueryToResponses(rows);
            
            // Enrich with favorite status if user is authenticated
            if (currentUser.isPresent()) {
                content = enrichWithFavoriteStatus(content, currentUser.get().getId());
            } else {
                content.forEach(response -> response.setIsFavorited(false));
            }

            int totalPages = (int) Math.ceil((double) totalElements / pageSize);
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("Fast listing query completed in {}ms - {} products", duration, content.size());

            return PaginationResponse.<ProductResponse>builder()
                    .content(content)
                    .pageNo(pageNo + 1) // Convert to 1-based
                    .pageSize(pageSize)
                    .totalElements(totalElements)
                    .totalPages(totalPages)
                    .first(pageNo == 0)
                    .last(pageNo >= totalPages - 1)
                    .hasNext(pageNo < totalPages - 1)
                    .hasPrevious(pageNo > 0)
                    .build();
                    
        } catch (Exception e) {
            log.error("Error in fast listing query: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve products", e);
        }
    }

    /**
     * Fast user favorites with native queries
     */
    public PaginationResponse<ProductResponse> getUserFavoritesWithNativeQuery(
            ProductFilterRequest filter,
            UUID userId) {

        long startTime = System.currentTimeMillis();
        
        try {
            // Calculate pagination
            int pageNo = filter.getPageNo() != null && filter.getPageNo() > 0 ? filter.getPageNo() - 1 : 0;
            int pageSize = filter.getPageSize() != null ? filter.getPageSize() : 10;
            int offset = pageNo * pageSize;

            // Execute native queries
            List<Object[]> rows = productRepository.findUserFavoriteProducts(userId, pageSize, offset);
            long totalElements = productRepository.countUserFavorites(userId);

            // Convert native query results to DTOs
            List<ProductResponse> content = mapNativeQueryToResponses(rows);
            content.forEach(response -> response.setIsFavorited(true)); // All are favorites

            int totalPages = (int) Math.ceil((double) totalElements / pageSize);
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("Fast favorites query completed in {}ms - {} products", duration, content.size());

            return PaginationResponse.<ProductResponse>builder()
                    .content(content)
                    .pageNo(pageNo + 1)
                    .pageSize(pageSize)
                    .totalElements(totalElements)
                    .totalPages(totalPages)
                    .first(pageNo == 0)
                    .last(pageNo >= totalPages - 1)
                    .hasNext(pageNo < totalPages - 1)
                    .hasPrevious(pageNo > 0)
                    .build();
                    
        } catch (Exception e) {
            log.error("Error in fast favorites query: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve favorites", e);
        }
    }

    /**
     * Convert native query result Object[] to ProductResponse
     * Array indices match the SELECT clause in repository queries
     */
    public ProductResponse mapNativeQueryToResponse(Object[] row) {
        try {
            ProductResponse response = new ProductResponse();
            
            // Basic fields (indices 0-13)
            response.setId((UUID) row[0]);
            response.setBusinessId((UUID) row[1]);
            response.setCategoryId((UUID) row[2]);
            response.setBrandId((UUID) row[3]);
            response.setName((String) row[4]);
            response.setDescription((String) row[5]);
            response.setStatus(ProductStatus.valueOf((String) row[6]));
            response.setPrice((BigDecimal) row[7]);
            response.setPromotionType((String) row[8]);
            response.setPromotionValue((BigDecimal) row[9]);
            response.setViewCount(getLongValue(row[10]));
            response.setFavoriteCount(getLongValue(row[11]));
            response.setCreatedAt(convertToLocalDateTime(row[12]));
            response.setUpdatedAt(convertToLocalDateTime(row[13]));
            
            // Related entity names (indices 14-16)
            response.setBusinessName((String) row[14]);
            response.setCategoryName((String) row[15]);
            response.setBrandName((String) row[16]);
            
            // Calculated fields (indices 17-20)
            response.setMainImageUrl((String) row[17]);
            response.setHasSizes((Boolean) row[18]);
            response.setDisplayPrice((BigDecimal) row[19]);
            response.setHasPromotionActive((Boolean) row[20]);
            
            // Set derived fields
            response.setPublicUrl("/products/" + response.getId());
            response.setImages(List.of()); // Empty for listings
            response.setSizes(List.of()); // Empty for listings
            response.setIsFavorited(false); // Set separately
            
            return response;
            
        } catch (Exception e) {
            log.error("Error mapping native query result: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to map query result", e);
        }
    }

    /**
     * Convert list of native query results to ProductResponse list
     */
    public List<ProductResponse> mapNativeQueryToResponses(List<Object[]> rows) {
        return rows.stream()
                .map(this::mapNativeQueryToResponse)
                .collect(Collectors.toList());
    }

    // ================================
    // RESPONSE MAPPING WITH BUSINESS LOGIC (FOR SINGLE PRODUCT VIEW)
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
    @Mapping(target = "isFavorited", constant = "false")
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

    protected String extractMainImageUrl(Product product) {
        if (product.getImages() == null || product.getImages().isEmpty()) {
            return null;
        }

        return product.getImages().stream()
                .filter(img -> img.getImageType().name().equals("MAIN"))
                .findFirst()
                .map(ProductImage::getImageUrl)
                .orElse(product.getImages().get(0).getImageUrl());
    }

    protected BigDecimal calculateDisplayPrice(Product product) {
        if (hasSizes(product)) {
            return product.getSizes().stream()
                    .map(this::calculateSizeFinalPrice)
                    .min(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);
        } else {
            return calculateProductFinalPrice(product);
        }
    }

    protected Boolean hasActivePromotion(Product product) {
        if (hasSizes(product)) {
            return product.getSizes().stream()
                    .anyMatch(this::isSizePromotionActive);
        } else {
            return isProductPromotionActive(product);
        }
    }

    protected Boolean hasSizes(Product product) {
        return product.getSizes() != null && !product.getSizes().isEmpty();
    }

    protected String generatePublicUrl(Product product) {
        return "/products/" + product.getId();
    }

    protected List<ProductImageResponse> sortImages(List<ProductImage> images) {
        if (images == null || images.isEmpty()) {
            return List.of();
        }

        return images.stream()
                .sorted((img1, img2) -> {
                    if (img1.getImageType().name().equals("MAIN") && !img2.getImageType().name().equals("MAIN")) {
                        return -1;
                    }
                    if (!img1.getImageType().name().equals("MAIN") && img2.getImageType().name().equals("MAIN")) {
                        return 1;
                    }
                    return img2.getCreatedAt().compareTo(img1.getCreatedAt());
                })
                .map(this::mapImageToResponse)
                .collect(Collectors.toList());
    }

    protected List<ProductSizeResponse> sortSizes(List<ProductSize> sizes) {
        if (sizes == null || sizes.isEmpty()) {
            return List.of();
        }

        return sizes.stream()
                .sorted((size1, size2) -> {
                    BigDecimal price1 = calculateSizeFinalPrice(size1);
                    BigDecimal price2 = calculateSizeFinalPrice(size2);
                    return price1.compareTo(price2);
                })
                .map(this::mapSizeToResponse)
                .collect(Collectors.toList());
    }

    // ================================
    // USER-SPECIFIC DATA ENRICHMENT
    // ================================

    /**
     * Enrich responses with favorite status for authenticated user
     */
    public List<ProductResponse> enrichWithFavoriteStatus(List<ProductResponse> responses, UUID userId) {
        if (responses.isEmpty()) {
            return responses;
        }

        try {
            List<UUID> productIds = responses.stream()
                    .map(ProductResponse::getId)
                    .collect(Collectors.toList());

            Set<UUID> favoriteProductIds = productFavoriteRepository.findAll().stream()
                    .filter(fav -> fav.getUserId().equals(userId) && 
                                 productIds.contains(fav.getProductId()) && 
                                 !fav.getIsDeleted())
                    .map(ProductFavorite::getProductId)
                    .collect(Collectors.toSet());

            responses.forEach(response -> 
                    response.setIsFavorited(favoriteProductIds.contains(response.getId())));

            return responses;
        } catch (Exception e) {
            log.warn("Error enriching with favorite status: {}", e.getMessage());
            responses.forEach(response -> response.setIsFavorited(false));
            return responses;
        }
    }

    /**
     * Enrich single response with favorite status
     */
    public ProductResponse enrichWithFavoriteStatus(ProductResponse response, UUID userId) {
        boolean isFavorited = productFavoriteRepository.existsByUserIdAndProductId(userId, response.getId());
        response.setIsFavorited(isFavorited);
        return response;
    }

    // ================================
    // HELPER CALCULATION METHODS
    // ================================

    private BigDecimal calculateSizeFinalPrice(ProductSize size) {
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

    private boolean isSizePromotionActive(ProductSize size) {
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

    private ProductImageResponse mapImageToResponse(ProductImage image) {
        ProductImageResponse response = new ProductImageResponse();
        response.setId(image.getId());
        response.setImageUrl(image.getImageUrl());
        response.setImageType(image.getImageType().name());
        return response;
    }

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
    // UTILITY METHODS
    // ================================

    private LocalDateTime convertToLocalDateTime(Object timestamp) {
        if (timestamp == null) {
            return null;
        }
        
        if (timestamp instanceof Timestamp) {
            return ((Timestamp) timestamp).toLocalDateTime();
        } else if (timestamp instanceof LocalDateTime) {
            return (LocalDateTime) timestamp;
        } else if (timestamp instanceof java.sql.Date) {
            return ((java.sql.Date) timestamp).toLocalDate().atStartOfDay();
        } else {
            log.warn("Unexpected timestamp type: {}", timestamp.getClass());
            return null;
        }
    }

    private Long getLongValue(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return 0L;
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
}
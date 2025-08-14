package com.emenu.features.product.mapper;

import com.emenu.enums.product.PromotionType;
import com.emenu.features.product.dto.request.ProductSizeRequest;
import com.emenu.features.product.dto.response.ProductSizeResponse;
import com.emenu.features.product.models.ProductSize;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class ProductSizeMapper {

    // ================================
    // ENTITY CREATION MAPPING
    // ================================

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "productId", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(source = "promotionType", target = "promotionType", qualifiedByName = "stringToPromotionType")
    public abstract ProductSize toEntity(ProductSizeRequest request);

    // ================================
    // RESPONSE MAPPING WITH CALCULATED FIELDS
    // ================================

    @Mapping(source = "promotionType", target = "promotionType", qualifiedByName = "promotionTypeToString")
    @Mapping(target = "finalPrice", expression = "java(calculateFinalPrice(productSize))")
    @Mapping(target = "isPromotionActive", expression = "java(isPromotionActive(productSize))")
    public abstract ProductSizeResponse toResponse(ProductSize productSize);

    /**
     * Convert list of ProductSize entities to sorted response list
     * Sorted by final price (lowest to highest)
     */
    public List<ProductSizeResponse> toSortedResponseList(List<ProductSize> productSizes) {
        if (productSizes == null || productSizes.isEmpty()) {
            return List.of();
        }

        return productSizes.stream()
                .sorted(createSizeComparator())
                .map(this::toResponse)
                .toList();
    }

    /**
     * Convert list without sorting (for backward compatibility)
     */
    public abstract List<ProductSizeResponse> toResponseList(List<ProductSize> productSizes);

    // ================================
    // SIZE MANAGEMENT UTILITIES
    // ================================

    /**
     * Create size entities from requests with promotion validation
     */
    public List<ProductSize> createSizeEntities(List<ProductSizeRequest> sizeRequests, 
                                               UUID productId) {
        if (sizeRequests == null || sizeRequests.isEmpty()) {
            return List.of();
        }

        return sizeRequests.stream()
                .map(request -> {
                    ProductSize size = toEntity(request);
                    size.setProductId(productId);
                    validateAndSetPromotion(size, request);
                    return size;
                })
                .toList();
    }

    /**
     * Update existing sizes with smart CRUD logic
     */
    public List<ProductSize> updateSizeEntities(List<ProductSizeRequest> sizeRequests,
                                               List<ProductSize> existingSizes,
                                               UUID productId) {
        if (sizeRequests == null || sizeRequests.isEmpty()) {
            return List.of();
        }

        List<ProductSize> updatedSizes = new java.util.ArrayList<>();

        for (ProductSizeRequest request : sizeRequests) {
            ProductSize size;
            
            if (request.getId() != null) {
                // Find existing size
                size = existingSizes.stream()
                        .filter(existing -> existing.getId().equals(request.getId()))
                        .findFirst()
                        .orElseGet(() -> {
                            ProductSize newSize = toEntity(request);
                            newSize.setProductId(productId);
                            return newSize;
                        });
                
                // Update fields
                updateSizeFromRequest(size, request);
            } else {
                // Create new size
                size = toEntity(request);
                size.setProductId(productId);
                validateAndSetPromotion(size, request);
            }

            updatedSizes.add(size);
        }

        return updatedSizes;
    }

    /**
     * Find sizes that should be deleted (existing but not in request)
     */
    public List<UUID> findSizesToDelete(List<ProductSizeRequest> sizeRequests,
                                       List<ProductSize> existingSizes) {
        if (existingSizes == null || existingSizes.isEmpty()) {
            return List.of();
        }

        java.util.Set<UUID> requestedIds = sizeRequests.stream()
                .map(ProductSizeRequest::getId)
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());

        return existingSizes.stream()
                .map(ProductSize::getId)
                .filter(id -> !requestedIds.contains(id))
                .toList();
    }

    /**
     * Get sorted sizes by final price
     */
    public List<ProductSize> getSortedSizes(List<ProductSize> sizes) {
        if (sizes == null || sizes.isEmpty()) {
            return List.of();
        }

        return sizes.stream()
                .sorted(createSizeComparator())
                .toList();
    }

    // ================================
    // BUSINESS LOGIC METHODS
    // ================================

    /**
     * Calculate final price considering promotions
     */
    protected BigDecimal calculateFinalPrice(ProductSize productSize) {
        if (!isPromotionActive(productSize)) {
            return productSize.getPrice();
        }

        BigDecimal basePrice = productSize.getPrice();
        PromotionType promotionType = productSize.getPromotionType();
        BigDecimal promotionValue = productSize.getPromotionValue();

        if (promotionType == null || promotionValue == null) {
            return basePrice;
        }

        return switch (promotionType) {
            case PERCENTAGE -> {
                BigDecimal discount = basePrice.multiply(promotionValue)
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                yield basePrice.subtract(discount);
            }
            case FIXED_AMOUNT -> {
                BigDecimal finalPrice = basePrice.subtract(promotionValue);
                yield finalPrice.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : finalPrice;
            }
        };
    }

    /**
     * Check if promotion is currently active
     */
    public Boolean isPromotionActive(ProductSize productSize) {
        if (productSize.getPromotionValue() == null || productSize.getPromotionType() == null) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        
        if (productSize.getPromotionFromDate() != null && now.isBefore(productSize.getPromotionFromDate())) {
            return false;
        }
        
        if (productSize.getPromotionToDate() != null && now.isAfter(productSize.getPromotionToDate())) {
            return false;
        }
        
        return true;
    }

    /**
     * Calculate discount amount for a size
     */
    public BigDecimal calculateDiscountAmount(ProductSize productSize) {
        if (!isPromotionActive(productSize)) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal basePrice = productSize.getPrice();
        BigDecimal finalPrice = calculateFinalPrice(productSize);
        return basePrice.subtract(finalPrice);
    }

    /**
     * Get lowest price from sizes list
     */
    public BigDecimal getLowestPrice(List<ProductSize> sizes) {
        if (sizes == null || sizes.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return sizes.stream()
                .map(this::calculateFinalPrice)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }

    /**
     * Get highest price from sizes list
     */
    public BigDecimal getHighestPrice(List<ProductSize> sizes) {
        if (sizes == null || sizes.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return sizes.stream()
                .map(this::calculateFinalPrice)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }

    /**
     * Check if any size has active promotion
     */
    public boolean hasAnyActivePromotion(List<ProductSize> sizes) {
        if (sizes == null || sizes.isEmpty()) {
            return false;
        }

        return sizes.stream()
                .anyMatch(this::isPromotionActive);
    }

    /**
     * Count sizes with active promotions
     */
    public long countActivePromotions(List<ProductSize> sizes) {
        if (sizes == null || sizes.isEmpty()) {
            return 0;
        }

        return sizes.stream()
                .filter(this::isPromotionActive)
                .count();
    }

    /**
     * Remove promotions from all sizes
     */
    public List<ProductSize> removeAllPromotions(List<ProductSize> sizes) {
        if (sizes == null || sizes.isEmpty()) {
            return sizes;
        }

        sizes.forEach(this::removePromotion);
        return sizes;
    }

    // ================================
    // HELPER METHODS
    // ================================

    /**
     * Create comparator for size sorting by final price (lowest to highest)
     */
    private Comparator<ProductSize> createSizeComparator() {
        return (size1, size2) -> {
            BigDecimal price1 = calculateFinalPrice(size1);
            BigDecimal price2 = calculateFinalPrice(size2);
            
            int priceComparison = price1.compareTo(price2);
            
            // If prices are equal, sort by name alphabetically
            if (priceComparison == 0) {
                String name1 = size1.getName() != null ? size1.getName() : "";
                String name2 = size2.getName() != null ? size2.getName() : "";
                return name1.compareToIgnoreCase(name2);
            }
            
            return priceComparison;
        };
    }

    /**
     * Update size entity from request
     */
    private void updateSizeFromRequest(ProductSize size, ProductSizeRequest request) {
        size.setName(request.getName());
        size.setPrice(request.getPrice());
        validateAndSetPromotion(size, request);
    }

    /**
     * Validate and set promotion from request
     */
    private void validateAndSetPromotion(ProductSize size, ProductSizeRequest request) {
        if (request.getPromotionType() != null && !request.getPromotionType().trim().isEmpty()) {
            try {
                PromotionType promotionType = PromotionType.valueOf(request.getPromotionType().toUpperCase());
                size.setPromotionType(promotionType);
                size.setPromotionValue(request.getPromotionValue());
                size.setPromotionFromDate(request.getPromotionFromDate());
                size.setPromotionToDate(request.getPromotionToDate());
            } catch (IllegalArgumentException e) {
                // Invalid promotion type, clear promotion
                removePromotion(size);
            }
        } else {
            removePromotion(size);
        }
    }

    /**
     * Remove promotion from size
     */
    private void removePromotion(ProductSize size) {
        size.setPromotionType(null);
        size.setPromotionValue(null);
        size.setPromotionFromDate(null);
        size.setPromotionToDate(null);
    }

    // ================================
    // MAPSTRUCT QUALIFIERS
    // ================================

    @Named("stringToPromotionType")
    protected PromotionType stringToPromotionType(String promotionType) {
        if (promotionType == null || promotionType.trim().isEmpty()) return null;
        try {
            return PromotionType.valueOf(promotionType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Named("promotionTypeToString")
    protected String promotionTypeToString(PromotionType promotionType) {
        return promotionType != null ? promotionType.name() : null;
    }

    // ================================
    // BATCH OPERATIONS
    // ================================

    /**
     * Process size requests for creation
     */
    public SizeCreationResult processSizeCreation(List<ProductSizeRequest> sizeRequests, 
                                                 UUID productId) {
        List<ProductSize> createdSizes = createSizeEntities(sizeRequests, productId);
        BigDecimal lowestPrice = getLowestPrice(createdSizes);
        BigDecimal highestPrice = getHighestPrice(createdSizes);
        boolean hasPromotions = hasAnyActivePromotion(createdSizes);
        
        return new SizeCreationResult(createdSizes, lowestPrice, highestPrice, hasPromotions);
    }

    /**
     * Process size requests for update
     */
    public SizeUpdateResult processSizeUpdate(List<ProductSizeRequest> sizeRequests,
                                             List<ProductSize> existingSizes,
                                             UUID productId) {
        List<ProductSize> updatedSizes = updateSizeEntities(sizeRequests, existingSizes, productId);
        List<UUID> sizesToDelete = findSizesToDelete(sizeRequests, existingSizes);
        BigDecimal lowestPrice = getLowestPrice(updatedSizes);
        BigDecimal highestPrice = getHighestPrice(updatedSizes);
        boolean hasPromotions = hasAnyActivePromotion(updatedSizes);
        
        return new SizeUpdateResult(updatedSizes, sizesToDelete, lowestPrice, highestPrice, hasPromotions);
    }

    // ================================
    // RESULT CLASSES
    // ================================

    public static class SizeCreationResult {
        public final List<ProductSize> sizes;
        public final BigDecimal lowestPrice;
        public final BigDecimal highestPrice;
        public final boolean hasPromotions;

        public SizeCreationResult(List<ProductSize> sizes, BigDecimal lowestPrice, 
                                BigDecimal highestPrice, boolean hasPromotions) {
            this.sizes = sizes;
            this.lowestPrice = lowestPrice;
            this.highestPrice = highestPrice;
            this.hasPromotions = hasPromotions;
        }
    }

    public static class SizeUpdateResult {
        public final List<ProductSize> sizes;
        public final List<UUID> sizesToDelete;
        public final BigDecimal lowestPrice;
        public final BigDecimal highestPrice;
        public final boolean hasPromotions;

        public SizeUpdateResult(List<ProductSize> sizes, List<UUID> sizesToDelete,
                              BigDecimal lowestPrice, BigDecimal highestPrice, boolean hasPromotions) {
            this.sizes = sizes;
            this.sizesToDelete = sizesToDelete;
            this.lowestPrice = lowestPrice;
            this.highestPrice = highestPrice;
            this.hasPromotions = hasPromotions;
        }
    }
}
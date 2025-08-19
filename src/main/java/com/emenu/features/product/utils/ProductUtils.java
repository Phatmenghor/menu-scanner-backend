package com.emenu.features.product.utils;

import com.emenu.enums.product.ProductStatus;
import com.emenu.enums.product.PromotionType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

@Component
@Slf4j
public class ProductUtils {

    private static final Pattern PRODUCT_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9\\s\\-_.()&]+$");
    private static final int MAX_PRODUCT_NAME_LENGTH = 255;
    private static final int MAX_DESCRIPTION_LENGTH = 5000;
    private static final BigDecimal MAX_PRICE = new BigDecimal("999999.99");
    private static final BigDecimal MIN_PRICE = BigDecimal.ZERO;

    /**
     * Validate product name
     */
    public boolean isValidProductName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        
        String trimmedName = name.trim();
        return trimmedName.length() <= MAX_PRODUCT_NAME_LENGTH 
               && PRODUCT_NAME_PATTERN.matcher(trimmedName).matches();
    }

    /**
     * Validate product description
     */
    public boolean isValidDescription(String description) {
        if (description == null) {
            return true; // Description is optional
        }
        return description.length() <= MAX_DESCRIPTION_LENGTH;
    }

    /**
     * Validate price range
     */
    public boolean isValidPrice(BigDecimal price) {
        if (price == null) {
            return false;
        }
        return price.compareTo(MIN_PRICE) >= 0 && price.compareTo(MAX_PRICE) <= 0;
    }

    /**
     * Calculate promotion discount
     */
    public BigDecimal calculatePromotionDiscount(BigDecimal originalPrice, 
                                                PromotionType promotionType,
                                                BigDecimal promotionValue) {
        if (originalPrice == null || promotionType == null || promotionValue == null) {
            return BigDecimal.ZERO;
        }

        return switch (promotionType) {
            case PERCENTAGE -> originalPrice.multiply(promotionValue)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            case FIXED_AMOUNT -> promotionValue.min(originalPrice);
        };
    }

    /**
     * Calculate final price after promotion
     */
    public BigDecimal calculateFinalPrice(BigDecimal originalPrice, 
                                         PromotionType promotionType, 
                                         BigDecimal promotionValue,
                                         LocalDateTime promotionFromDate,
                                         LocalDateTime promotionToDate) {
        if (!isPromotionActive(promotionFromDate, promotionToDate)) {
            return originalPrice;
        }

        BigDecimal discount = calculatePromotionDiscount(originalPrice, promotionType, promotionValue);
        BigDecimal finalPrice = originalPrice.subtract(discount);
        
        // Ensure final price is not negative
        return finalPrice.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : finalPrice;
    }

    /**
     * Check if promotion is currently active
     */
    public boolean isPromotionActive(LocalDateTime promotionFromDate, LocalDateTime promotionToDate) {
        LocalDateTime now = LocalDateTime.now();
        
        if (promotionFromDate != null && now.isBefore(promotionFromDate)) {
            return false;
        }
        
        if (promotionToDate != null && now.isAfter(promotionToDate)) {
            return false;
        }
        
        return true;
    }

    /**
     * Validate promotion values
     */
    public boolean isValidPromotion(PromotionType promotionType, BigDecimal promotionValue) {
        if (promotionType == null || promotionValue == null) {
            return false;
        }

        return switch (promotionType) {
            case PERCENTAGE -> promotionValue.compareTo(BigDecimal.ZERO) > 0 
                              && promotionValue.compareTo(BigDecimal.valueOf(100)) <= 0;
            case FIXED_AMOUNT -> promotionValue.compareTo(BigDecimal.ZERO) > 0 
                                && promotionValue.compareTo(MAX_PRICE) <= 0;
        };
    }

    /**
     * Sanitize product name
     */
    public String sanitizeProductName(String name) {
        if (name == null) {
            return null;
        }
        
        String sanitized = name.trim().replaceAll("\\s+", " ");
        return sanitized.length() > MAX_PRODUCT_NAME_LENGTH 
               ? sanitized.substring(0, MAX_PRODUCT_NAME_LENGTH) 
               : sanitized;
    }

    /**
     * Generate product SKU (Simple implementation)
     */
    public String generateProductSKU(String businessCode, String categoryCode, String productName) {
        if (businessCode == null || categoryCode == null || productName == null) {
            return "SKU-" + System.currentTimeMillis();
        }

        String cleanBusinessCode = businessCode.replaceAll("[^A-Z0-9]", "").substring(0, Math.min(3, businessCode.length()));
        String cleanCategoryCode = categoryCode.replaceAll("[^A-Z0-9]", "").substring(0, Math.min(3, categoryCode.length()));
        String cleanProductCode = productName.replaceAll("[^A-Z0-9]", "").substring(0, Math.min(4, productName.length()));
        
        return String.format("%s-%s-%s-%d", 
                cleanBusinessCode, 
                cleanCategoryCode, 
                cleanProductCode, 
                System.currentTimeMillis() % 10000);
    }

    /**
     * Calculate average rating (placeholder for future rating feature)
     */
    public BigDecimal calculateAverageRating(List<Integer> ratings) {
        if (ratings == null || ratings.isEmpty()) {
            return BigDecimal.ZERO;
        }

        double average = ratings.stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);

        return BigDecimal.valueOf(average).setScale(1, RoundingMode.HALF_UP);
    }

    /**
     * Check if product is eligible for promotion
     */
    public boolean isEligibleForPromotion(ProductStatus status, BigDecimal price) {
        return ProductStatus.ACTIVE.equals(status) && 
               price != null && 
               price.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Format price for display
     */
    public String formatPrice(BigDecimal price, String currency) {
        if (price == null) {
            return "0.00";
        }
        
        String formattedPrice = price.setScale(2, RoundingMode.HALF_UP).toString();
        return currency != null ? currency + " " + formattedPrice : formattedPrice;
    }

    /**
     * Validate image URL format
     */
    public boolean isValidImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return false;
        }
        
        String url = imageUrl.toLowerCase();
        return url.startsWith("http://") || url.startsWith("https://") || url.startsWith("/");
    }

    /**
     * Generate search keywords from product data
     */
    public String generateSearchKeywords(String productName, String description, 
                                       String categoryName, String brandName, String businessName) {
        StringBuilder keywords = new StringBuilder();
        
        if (productName != null) keywords.append(productName.toLowerCase()).append(" ");
        if (description != null) keywords.append(description.toLowerCase()).append(" ");
        if (categoryName != null) keywords.append(categoryName.toLowerCase()).append(" ");
        if (brandName != null) keywords.append(brandName.toLowerCase()).append(" ");
        if (businessName != null) keywords.append(businessName.toLowerCase()).append(" ");
        
        return keywords.toString().trim();
    }

    /**
     * Check if product has valid inventory (placeholder for future inventory feature)
     */
    public boolean hasValidInventory(Integer stockQuantity, Integer minStockLevel) {
        if (stockQuantity == null) {
            return true; // No inventory tracking
        }
        
        int minLevel = minStockLevel != null ? minStockLevel : 0;
        return stockQuantity >= minLevel;
    }

    /**
     * Calculate discount percentage for display
     */
    public int calculateDiscountPercentage(BigDecimal originalPrice, BigDecimal finalPrice) {
        if (originalPrice == null || finalPrice == null || 
            originalPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }

        BigDecimal discount = originalPrice.subtract(finalPrice);
        BigDecimal percentage = discount.divide(originalPrice, 4, RoundingMode.HALF_UP)
                                      .multiply(BigDecimal.valueOf(100));
        
        return percentage.intValue();
    }

    /**
     * Validate promotion date range
     */
    public boolean isValidPromotionDateRange(LocalDateTime fromDate, LocalDateTime toDate) {
        if (fromDate == null && toDate == null) {
            return true; // No date restrictions
        }
        
        if (fromDate != null && toDate != null) {
            return fromDate.isBefore(toDate);
        }
        
        return true; // One-sided date restriction is valid
    }

    /**
     * Log product operation
     */
    public void logProductOperation(String operation, String productName, String businessId) {
        log.info("Product Operation - {}: '{}' for business: {}", 
                operation, productName, businessId);
    }
}
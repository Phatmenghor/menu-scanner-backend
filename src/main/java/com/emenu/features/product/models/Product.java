package com.emenu.features.product.models;

import com.emenu.enums.product.ProductStatus;
import com.emenu.enums.product.PromotionType;
import com.emenu.features.auth.models.Business;
import com.emenu.features.business.models.Brand;
import com.emenu.features.business.models.Category;
import com.emenu.shared.domain.BaseUUIDEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "products")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Product extends BaseUUIDEntity {

    @Column(name = "business_id", nullable = false)
    private UUID businessId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", insertable = false, updatable = false)
    private Business business;

    @Column(name = "category_id", nullable = false)
    private UUID categoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", insertable = false, updatable = false)
    private Category category;

    @Column(name = "brand_id")
    private UUID brandId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", insertable = false, updatable = false)
    private Brand brand;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProductStatus status = ProductStatus.ACTIVE;

    // Price fields for products without sizes
    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    // Promotion fields for products without sizes
    @Enumerated(EnumType.STRING)
    @Column(name = "promotion_type")
    private PromotionType promotionType;

    @Column(name = "promotion_value", precision = 10, scale = 2)
    private BigDecimal promotionValue;

    @Column(name = "promotion_from_date")
    private LocalDateTime promotionFromDate;

    @Column(name = "promotion_to_date")
    private LocalDateTime promotionToDate;

    // Statistics
    @Column(name = "view_count")
    private Long viewCount = 0L;

    @Column(name = "favorite_count")
    private Long favoriteCount = 0L;

    // FIXED: Collections with proper orphan removal handling
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ProductImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ProductSize> sizes = new ArrayList<>();

    // ================================
    // COLLECTION MANAGEMENT METHODS - FIXED
    // ================================

    /**
     * FIXED: Properly set images collection with parent reference management
     */
    public void setImages(List<ProductImage> images) {
        if (this.images == null) {
            this.images = new ArrayList<>();
        }
        
        // Clear existing collection
        this.images.clear();
        
        // Add new images with proper parent reference
        if (images != null) {
            images.forEach(image -> {
                image.setProduct(this);
                image.setProductId(this.getId());
                this.images.add(image);
            });
        }
    }

    /**
     * FIXED: Properly set sizes collection with parent reference management
     */
    public void setSizes(List<ProductSize> sizes) {
        if (this.sizes == null) {
            this.sizes = new ArrayList<>();
        }
        
        // Clear existing collection
        this.sizes.clear();
        
        // Add new sizes with proper parent reference
        if (sizes != null) {
            sizes.forEach(size -> {
                size.setProduct(this);
                size.setProductId(this.getId());
                this.sizes.add(size);
            });
        }
    }

    /**
     * FIXED: Add single image with proper parent reference
     */
    public void addImage(ProductImage image) {
        if (this.images == null) {
            this.images = new ArrayList<>();
        }
        
        // Set parent reference
        image.setProduct(this);
        image.setProductId(this.getId());
        
        // Add to collection
        this.images.add(image);
    }

    /**
     * FIXED: Remove single image with proper cleanup
     */
    public void removeImage(ProductImage image) {
        if (this.images != null && this.images.contains(image)) {
            this.images.remove(image);
            // Clear parent reference
            image.setProduct(null);
            image.setProductId(null);
        }
    }

    /**
     * FIXED: Add single size with proper parent reference
     */
    public void addSize(ProductSize size) {
        if (this.sizes == null) {
            this.sizes = new ArrayList<>();
        }
        
        // Set parent reference
        size.setProduct(this);
        size.setProductId(this.getId());
        
        // Add to collection
        this.sizes.add(size);
    }

    /**
     * FIXED: Remove single size with proper cleanup
     */
    public void removeSize(ProductSize size) {
        if (this.sizes != null && this.sizes.contains(size)) {
            this.sizes.remove(size);
            // Clear parent reference
            size.setProduct(null);
            size.setProductId(null);
        }
    }

    /**
     * Clear all images
     */
    public void clearImages() {
        if (this.images != null) {
            // Clear parent references
            this.images.forEach(image -> {
                image.setProduct(null);
                image.setProductId(null);
            });
            this.images.clear();
        }
    }

    /**
     * Clear all sizes
     */
    public void clearSizes() {
        if (this.sizes != null) {
            // Clear parent references
            this.sizes.forEach(size -> {
                size.setProduct(null);
                size.setProductId(null);
            });
            this.sizes.clear();
        }
    }

    // ================================
    // BUSINESS METHODS
    // ================================

    public void activate() {
        this.status = ProductStatus.ACTIVE;
    }

    public void deactivate() {
        this.status = ProductStatus.INACTIVE;
    }

    public void markOutOfStock() {
        this.status = ProductStatus.OUT_OF_STOCK;
    }

    public boolean isActive() {
        return ProductStatus.ACTIVE.equals(status);
    }

    public boolean isAvailable() {
        return ProductStatus.ACTIVE.equals(status) || ProductStatus.OUT_OF_STOCK.equals(status);
    }

    public void incrementViewCount() {
        this.viewCount = (this.viewCount == null ? 0L : this.viewCount) + 1;
    }

    public void incrementFavoriteCount() {
        this.favoriteCount = (this.favoriteCount == null ? 0L : this.favoriteCount) + 1;
    }

    public void decrementFavoriteCount() {
        this.favoriteCount = Math.max(0L, (this.favoriteCount == null ? 0L : this.favoriteCount) - 1);
    }

    // ================================
    // PRICING LOGIC
    // ================================

    public boolean hasSizes() {
        return sizes != null && !sizes.isEmpty();
    }

    public BigDecimal getDisplayPrice() {
        if (hasSizes()) {
            // Get the lowest price from sizes (with active promotions considered)
            return sizes.stream()
                    .map(ProductSize::getFinalPrice)
                    .min(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);
        } else {
            // Use product-level price with promotion
            return getFinalPrice();
        }
    }

    public BigDecimal getStartingPrice() {
        return getDisplayPrice();
    }

    // Product-level promotion methods (for products without sizes)
    public BigDecimal getFinalPrice() {
        if (hasSizes()) {
            return getDisplayPrice(); // Delegate to sizes
        }

        if (!isPromotionActive()) {
            return this.price != null ? this.price : BigDecimal.ZERO;
        }

        BigDecimal basePrice = this.price != null ? this.price : BigDecimal.ZERO;

        switch (promotionType) {
            case PERCENTAGE -> {
                BigDecimal discount = basePrice.multiply(promotionValue).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                return basePrice.subtract(discount);
            }
            case FIXED_AMOUNT -> {
                BigDecimal finalPrice = basePrice.subtract(promotionValue);
                return finalPrice.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : finalPrice;
            }
            default -> {
                return basePrice;
            }
        }
    }

    public boolean isPromotionActive() {
        if (hasSizes()) {
            // Check if any size has active promotion
            return sizes.stream().anyMatch(ProductSize::isPromotionActive);
        }

        // Check product-level promotion
        if (promotionValue == null || promotionType == null) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        
        if (promotionFromDate != null && now.isBefore(promotionFromDate)) {
            return false;
        }
        
        if (promotionToDate != null && now.isAfter(promotionToDate)) {
            return false;
        }
        
        return true;
    }

    public void setPromotion(PromotionType type, BigDecimal value, LocalDateTime fromDate, LocalDateTime toDate) {
        this.promotionType = type;
        this.promotionValue = value;
        this.promotionFromDate = fromDate;
        this.promotionToDate = toDate;
    }

    public void removePromotion() {
        this.promotionType = null;
        this.promotionValue = null;
        this.promotionFromDate = null;
        this.promotionToDate = null;
    }

    public String getMainImageUrl() {
        if (images == null || images.isEmpty()) {
            return null;
        }
        return images.stream()
                .filter(ProductImage::isMain)
                .findFirst()
                .map(ProductImage::getImageUrl)
                .orElse(images.get(0).getImageUrl());
    }

    public boolean hasPromotion() {
        if (hasSizes()) {
            return sizes.stream().anyMatch(ProductSize::isPromotionActive);
        }
        return isPromotionActive();
    }

    public boolean hasMultipleSizes() {
        return sizes != null && sizes.size() > 1;
    }

    // ================================
    // COLLECTION GETTERS - FIXED
    // ================================

    /**
     * FIXED: Ensure collections are never null
     */
    public List<ProductImage> getImages() {
        if (this.images == null) {
            this.images = new ArrayList<>();
        }
        return this.images;
    }

    /**
     * FIXED: Ensure collections are never null
     */
    public List<ProductSize> getSizes() {
        if (this.sizes == null) {
            this.sizes = new ArrayList<>();
        }
        return this.sizes;
    }
}
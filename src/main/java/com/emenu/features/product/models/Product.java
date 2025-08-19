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
import org.hibernate.annotations.BatchSize;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "products", indexes = {
        // ✅ EXISTING: Keep all current comprehensive indexes (already optimized)
        @Index(name = "idx_products_business_status_deleted", columnList = "business_id, status, is_deleted"),
        @Index(name = "idx_products_business_category_deleted", columnList = "business_id, category_id, is_deleted"),
        @Index(name = "idx_products_business_brand_deleted", columnList = "business_id, brand_id, is_deleted"),
        @Index(name = "idx_products_business_created_deleted", columnList = "business_id, created_at, is_deleted"),
        @Index(name = "idx_products_status_created_deleted", columnList = "status, created_at, is_deleted"),
        @Index(name = "idx_products_category_created_deleted", columnList = "category_id, created_at, is_deleted"),
        @Index(name = "idx_products_brand_created_deleted", columnList = "brand_id, created_at, is_deleted"),
        @Index(name = "idx_products_name_deleted", columnList = "name, is_deleted"),
        @Index(name = "idx_products_price_deleted", columnList = "price, is_deleted"),
        @Index(name = "idx_products_promotion_dates", columnList = "promotion_from_date, promotion_to_date, is_deleted"),

        // ✅ FIXED: Additional BaseUUIDEntity indexes
        @Index(name = "idx_product_deleted", columnList = "is_deleted"),
        @Index(name = "idx_product_deleted_created", columnList = "is_deleted, created_at"),
        @Index(name = "idx_product_deleted_updated", columnList = "is_deleted, updated_at"),
        @Index(name = "idx_product_view_count_deleted", columnList = "view_count, is_deleted"),
        @Index(name = "idx_product_favorite_count_deleted", columnList = "favorite_count, is_deleted")
})
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

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProductStatus status = ProductStatus.ACTIVE;

    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "promotion_type")
    private PromotionType promotionType;

    @Column(name = "promotion_value", precision = 10, scale = 2)
    private BigDecimal promotionValue;

    @Column(name = "promotion_from_date")
    private LocalDateTime promotionFromDate;

    @Column(name = "promotion_to_date")
    private LocalDateTime promotionToDate;

    @Column(name = "view_count", nullable = false)
    private Long viewCount = 0L;

    @Column(name = "favorite_count", nullable = false)
    private Long favoriteCount = 0L;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @BatchSize(size = 25)
    @OrderBy("imageType ASC, createdAt DESC")
    private List<ProductImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @BatchSize(size = 25)
    @OrderBy("price ASC")
    private List<ProductSize> sizes = new ArrayList<>();

    // ================================
    // BUSINESS METHODS
    // ================================

    public boolean hasSizes() {
        return sizes != null && !sizes.isEmpty();
    }

    public BigDecimal getDisplayPrice() {
        if (hasSizes()) {
            return sizes.stream()
                    .map(ProductSize::getFinalPrice)
                    .min(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);
        }
        return getFinalPrice();
    }

    public BigDecimal getFinalPrice() {
        if (!isPromotionActive()) {
            return this.price != null ? this.price : BigDecimal.ZERO;
        }

        BigDecimal basePrice = this.price != null ? this.price : BigDecimal.ZERO;

        switch (promotionType) {
            case PERCENTAGE -> {
                BigDecimal discount = basePrice.multiply(promotionValue)
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
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

    public void incrementViewCount() {
        this.viewCount = (this.viewCount == null ? 0L : this.viewCount) + 1;
    }

    public void incrementFavoriteCount() {
        this.favoriteCount = (this.favoriteCount == null ? 0L : this.favoriteCount) + 1;
    }

    public void decrementFavoriteCount() {
        this.favoriteCount = Math.max(0L, (this.favoriteCount == null ? 0L : this.favoriteCount) - 1);
    }

    public List<ProductImage> getImages() {
        if (this.images == null) {
            this.images = new ArrayList<>();
        }
        return this.images;
    }

    public List<ProductSize> getSizes() {
        if (this.sizes == null) {
            this.sizes = new ArrayList<>();
        }
        return this.sizes;
    }

    public boolean isActive() {
        return ProductStatus.ACTIVE.equals(status);
    }

    public boolean isAvailable() {
        return ProductStatus.ACTIVE.equals(status) || ProductStatus.OUT_OF_STOCK.equals(status);
    }
}

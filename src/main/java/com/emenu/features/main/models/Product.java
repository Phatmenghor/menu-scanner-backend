package com.emenu.features.main.models;

import com.emenu.enums.product.ProductStatus;
import com.emenu.enums.product.PromotionType;
import com.emenu.features.auth.models.Business;
import com.emenu.shared.domain.BaseUUIDEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
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

    @Column(name = "display_price", precision = 10, scale = 2)
    private BigDecimal displayPrice;

    @Column(name = "display_origin_price", precision = 10, scale = 2)
    private BigDecimal displayOriginPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "display_promotion_type")
    private PromotionType displayPromotionType;

    @Column(name = "display_promotion_value", precision = 10, scale = 2)
    private BigDecimal displayPromotionValue;

    @Column(name = "display_promotion_from_date")
    private LocalDateTime displayPromotionFromDate;

    @Column(name = "display_promotion_to_date")
    private LocalDateTime displayPromotionToDate;

    @Column(name = "has_sizes", nullable = false)
    private Boolean hasSizes = false;

    @Column(name = "has_active_promotion", nullable = false)
    private Boolean hasActivePromotion = false;

    @Column(name = "view_count", nullable = false)
    private Long viewCount = 0L;

    @Column(name = "favorite_count", nullable = false)
    private Long favoriteCount = 0L;

    @Column(name = "main_image_url")
    private String mainImageUrl;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @OrderBy("createdAt DESC")
    private List<ProductImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @OrderBy("price ASC")
    private List<ProductSize> sizes = new ArrayList<>();

    public void syncDisplayFieldsFromSizes() {
        List<ProductSize> activeSizes = (sizes == null) ? List.of() : sizes.stream()
                .filter(size -> size != null && !size.getIsDeleted())
                .toList();

        if (activeSizes.isEmpty()) {
            // No active sizes - use product's own fields
            this.hasSizes = false;
            this.hasActivePromotion = isPromotionActive();
            this.displayOriginPrice = this.price;
            if (this.hasActivePromotion) {
                this.displayPrice = getFinalPrice();
                this.displayPromotionType = this.promotionType;
                this.displayPromotionValue = this.promotionValue;
                this.displayPromotionFromDate = this.promotionFromDate;
                this.displayPromotionToDate = this.promotionToDate;
            } else {
                this.displayPrice = this.price != null ? this.price : BigDecimal.ZERO;
                this.displayPromotionType = null;
                this.displayPromotionValue = null;
                this.displayPromotionFromDate = null;
                this.displayPromotionToDate = null;
            }
        } else {
            // Has active sizes - use size fields
            this.hasSizes = true;

            // hasActivePromotion = true if ANY size has an active promotion
            this.hasActivePromotion = activeSizes.stream().anyMatch(ProductSize::isPromotionActive);

            // Pick display size: cheapest promoted size first, otherwise cheapest overall
            ProductSize displaySize = activeSizes.stream()
                    .filter(ProductSize::isPromotionActive)
                    .min(Comparator.comparing(ProductSize::getPrice))
                    .orElseGet(() -> activeSizes.stream()
                            .min(Comparator.comparing(ProductSize::getPrice))
                            .orElse(null));

            if (displaySize != null) {
                this.displayOriginPrice = displaySize.getPrice();
                if (this.hasActivePromotion) {
                    this.displayPromotionType = displaySize.getPromotionType();
                    this.displayPromotionValue = displaySize.getPromotionValue();
                    this.displayPromotionFromDate = displaySize.getPromotionFromDate();
                    this.displayPromotionToDate = displaySize.getPromotionToDate();
                    this.displayPrice = displaySize.getFinalPrice();
                } else {
                    this.displayPromotionType = null;
                    this.displayPromotionValue = null;
                    this.displayPromotionFromDate = null;
                    this.displayPromotionToDate = null;
                    this.displayPrice = displaySize.getPrice();
                }
            }
        }
    }

    public void initializeDisplayFields() {
        this.hasSizes = false;
        this.hasActivePromotion = isPromotionActive();
        this.displayOriginPrice = this.price;
        if (this.hasActivePromotion) {
            this.displayPrice = getFinalPrice();
            this.displayPromotionType = this.promotionType;
            this.displayPromotionValue = this.promotionValue;
            this.displayPromotionFromDate = this.promotionFromDate;
            this.displayPromotionToDate = this.promotionToDate;
        } else {
            this.displayPrice = this.price != null ? this.price : BigDecimal.ZERO;
            this.displayPromotionType = null;
            this.displayPromotionValue = null;
            this.displayPromotionFromDate = null;
            this.displayPromotionToDate = null;
        }
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

        LocalDateTime today = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS);

        if (promotionFromDate != null && today.isBefore(promotionFromDate.truncatedTo(ChronoUnit.DAYS))) {
            return false;
        }

        if (promotionToDate != null && today.isAfter(promotionToDate.truncatedTo(ChronoUnit.DAYS))) {
            return false;
        }

        return true;
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
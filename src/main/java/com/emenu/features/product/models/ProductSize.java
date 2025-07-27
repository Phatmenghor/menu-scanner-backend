package com.emenu.features.product.models;

import com.emenu.enums.product.PromotionType;
import com.emenu.shared.domain.BaseUUIDEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Entity
@Table(name = "product_sizes")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ProductSize extends BaseUUIDEntity {

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private Product product;

    @Column(name = "name", nullable = false)
    private String name; // Small, Medium, Large, etc.

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "has_promotion", nullable = false)
    private Boolean hasPromotion = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "promotion_type")
    private PromotionType promotionType;

    @Column(name = "promotion_value", precision = 10, scale = 2)
    private BigDecimal promotionValue;

    @Column(name = "final_price", precision = 10, scale = 2)
    private BigDecimal finalPrice;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    // Business Methods
    public void calculateFinalPrice() {
        if (!Boolean.TRUE.equals(hasPromotion) || promotionValue == null || promotionType == null) {
            this.finalPrice = this.price;
            return;
        }

        switch (promotionType) {
            case PERCENTAGE -> {
                BigDecimal discount = price.multiply(promotionValue).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                this.finalPrice = price.subtract(discount);
            }
            case FIXED_AMOUNT -> {
                this.finalPrice = price.subtract(promotionValue);
                if (this.finalPrice.compareTo(BigDecimal.ZERO) < 0) {
                    this.finalPrice = BigDecimal.ZERO;
                }
            }
        }
    }

    public void setPromotion(PromotionType type, BigDecimal value) {
        this.promotionType = type;
        this.promotionValue = value;
        this.hasPromotion = true;
        calculateFinalPrice();
    }

    public void removePromotion() {
        this.hasPromotion = false;
        this.promotionType = null;
        this.promotionValue = null;
        this.finalPrice = this.price;
    }

    public BigDecimal getDiscountAmount() {
        if (!Boolean.TRUE.equals(hasPromotion)) {
            return BigDecimal.ZERO;
        }
        return price.subtract(finalPrice);
    }

    // Ensure final price is calculated when price changes
    @PrePersist
    @PreUpdate
    public void prePersist() {
        calculateFinalPrice();
    }
}
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
import java.time.LocalDateTime;
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

    // Promotion fields
    @Enumerated(EnumType.STRING)
    @Column(name = "promotion_type")
    private PromotionType promotionType;

    @Column(name = "promotion_value", precision = 10, scale = 2)
    private BigDecimal promotionValue;

    @Column(name = "promotion_from_date")
    private LocalDateTime promotionFromDate;

    @Column(name = "promotion_to_date")
    private LocalDateTime promotionToDate;

    // Business Methods
    public BigDecimal getFinalPrice() {
        if (!isPromotionActive()) {
            return this.price;
        }

        switch (promotionType) {
            case PERCENTAGE -> {
                BigDecimal discount = price.multiply(promotionValue).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                return price.subtract(discount);
            }
            case FIXED_AMOUNT -> {
                BigDecimal finalPrice = price.subtract(promotionValue);
                return finalPrice.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : finalPrice;
            }
            default -> {
                return this.price;
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

    public BigDecimal getDiscountAmount() {
        if (!isPromotionActive()) {
            return BigDecimal.ZERO;
        }
        return price.subtract(getFinalPrice());
    }
}
package com.emenu.features.order.models;

import com.emenu.features.product.models.Product;
import com.emenu.features.product.models.ProductSize;
import com.emenu.shared.domain.BaseUUIDEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "cart_items")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class CartItem extends BaseUUIDEntity {

    @Column(name = "cart_id", nullable = false)
    private UUID cartId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", insertable = false, updatable = false)
    private Cart cart;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private Product product;

    @Column(name = "product_size_id", nullable = false)
    private UUID productSizeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_size_id", insertable = false, updatable = false)
    private ProductSize productSize;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice; // Original price

    @Column(name = "final_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal finalPrice; // Price after promotion

    @Column(name = "notes")
    private String notes;

    // Business Methods
    public BigDecimal getTotalPrice() {
        return finalPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public BigDecimal getDiscountAmount() {
        return unitPrice.subtract(finalPrice).multiply(BigDecimal.valueOf(quantity));
    }

    public Boolean hasDiscount() {
        return unitPrice.compareTo(finalPrice) > 0;
    }

    // Constructor for creating cart item
    public CartItem(UUID cartId, UUID productId, UUID productSizeId, Integer quantity, 
                    BigDecimal unitPrice, BigDecimal finalPrice, String notes) {
        this.cartId = cartId;
        this.productId = productId;
        this.productSizeId = productSizeId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.finalPrice = finalPrice;
        this.notes = notes;
    }
}
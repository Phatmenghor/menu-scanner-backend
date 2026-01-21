package com.emenu.features.order.models;

import com.emenu.features.main.models.Product;
import com.emenu.features.main.models.ProductSize;
import com.emenu.shared.domain.BaseUUIDEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "cart_items", indexes = {
        // ✅ FIXED: BaseUUIDEntity indexes
        @Index(name = "idx_cart_item_deleted", columnList = "is_deleted"),
        @Index(name = "idx_cart_item_deleted_created", columnList = "is_deleted, created_at"),

        // ✅ FIXED: Relationship indexes
        @Index(name = "idx_cart_item_cart_deleted", columnList = "cart_id, is_deleted"),
        @Index(name = "idx_cart_item_product_deleted", columnList = "product_id, is_deleted"),
        @Index(name = "idx_cart_item_size_deleted", columnList = "product_size_id, is_deleted"),
        @Index(name = "idx_cart_item_cart_product_deleted", columnList = "cart_id, product_id, is_deleted")
})
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

    @Column(name = "product_size_id")
    private UUID productSizeId; // Nullable for products without sizes

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_size_id", insertable = false, updatable = false)
    private ProductSize productSize;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    // Business Methods - Always get current pricing from product/size
    public BigDecimal getCurrentPrice() {
        if (productSize != null) {
            return productSize.getPrice();
        } else if (product != null) {
            return product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO;
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal getFinalPrice() {
        // Always get current final price with active promotions
        if (productSize != null) {
            return productSize.getFinalPrice();
        } else if (product != null) {
            return product.getFinalPrice();
        }
        return getCurrentPrice();
    }

    public BigDecimal getTotalPrice() {
        return getFinalPrice().multiply(BigDecimal.valueOf(quantity));
    }

    public Boolean hasDiscount() {
        return getCurrentPrice().compareTo(getFinalPrice()) > 0;
    }

    public String getSizeName() {
        return productSize != null ? productSize.getName() : "Standard";
    }

    // NEW: Added missing method for compilation
    public BigDecimal getUnitPrice() {
        return getCurrentPrice();
    }

    // ✅ UPDATED: Single availability check that combines both availability and stock status
    public Boolean isAvailable() {
        if (product == null) return false;
        return product.isActive() && !product.getIsDeleted();
    }

    // ✅ DEPRECATED: Keep these methods for backward compatibility but use the single isAvailable() method
    @Deprecated
    public Boolean isProductAvailable() {
        return isAvailable();
    }

    @Deprecated
    public Boolean isProductInStock() {
        if (product == null) return false;
        return product.isAvailable(); // ACTIVE or OUT_OF_STOCK
    }

    public String getUnavailabilityReason() {
        if (product == null) return "Product not found";
        if (product.getIsDeleted()) return "Product has been removed";
        if (!product.isActive()) return "Product is no longer available";
        return null;
    }

    // Constructor for creating cart item (no price storage)
    public CartItem(UUID cartId, UUID productId, UUID productSizeId, Integer quantity) {
        this.cartId = cartId;
        this.productId = productId;
        this.productSizeId = productSizeId;
        this.quantity = quantity;
    }
}
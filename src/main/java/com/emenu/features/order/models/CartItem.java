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

    @Column(name = "product_size_id")
    private UUID productSizeId; // Nullable for products without sizes

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_size_id", insertable = false, updatable = false)
    private ProductSize productSize;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice; // Original price at time of adding to cart

    // Business Methods
    public BigDecimal getFinalPrice() {
        // Calculate current final price (with active promotions)
        if (productSize != null) {
            return productSize.getFinalPrice();
        } else if (product != null) {
            return product.getFinalPrice();
        }
        return unitPrice; // Fallback to stored price
    }

    public BigDecimal getTotalPrice() {
        return getFinalPrice().multiply(BigDecimal.valueOf(quantity));
    }

    public BigDecimal getDiscountAmount() {
        return unitPrice.subtract(getFinalPrice()).multiply(BigDecimal.valueOf(quantity));
    }

    public Boolean hasDiscount() {
        return unitPrice.compareTo(getFinalPrice()) > 0;
    }

    public String getSizeName() {
        return productSize != null ? productSize.getName() : "Standard";
    }

    // Constructor for creating cart item
    public CartItem(UUID cartId, UUID productId, UUID productSizeId, Integer quantity, BigDecimal unitPrice) {
        this.cartId = cartId;
        this.productId = productId;
        this.productSizeId = productSizeId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }
}
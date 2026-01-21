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
@Table(name = "order_items", indexes = {
        // ✅ FIXED: BaseUUIDEntity indexes
        @Index(name = "idx_order_item_deleted", columnList = "is_deleted"),
        @Index(name = "idx_order_item_deleted_created", columnList = "is_deleted, created_at"),

        // ✅ FIXED: Relationship indexes
        @Index(name = "idx_order_item_order_deleted", columnList = "order_id, is_deleted"),
        @Index(name = "idx_order_item_product_deleted", columnList = "product_id, is_deleted"),
        @Index(name = "idx_order_item_size_deleted", columnList = "product_size_id, is_deleted"),
        @Index(name = "idx_order_item_order_product_deleted", columnList = "order_id, product_id, is_deleted")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem extends BaseUUIDEntity {

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", insertable = false, updatable = false)
    private Order order;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private Product product;

    @Column(name = "product_size_id")
    private UUID productSizeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_size_id", insertable = false, updatable = false)
    private ProductSize productSize;

    // Snapshot of product details at time of order
    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "product_image_url")
    private String productImageUrl;

    @Column(name = "size_name")
    private String sizeName; // "Standard" if no size

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice; // Price at time of order

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice; // unitPrice * quantity

    // Business Methods
    public void calculateTotalPrice() {
        this.totalPrice = this.unitPrice.multiply(BigDecimal.valueOf(this.quantity));
    }
}
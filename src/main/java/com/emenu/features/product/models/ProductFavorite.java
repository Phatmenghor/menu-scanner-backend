package com.emenu.features.product.models;

import com.emenu.features.auth.models.User;
import com.emenu.shared.domain.BaseUUIDEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "product_favorites",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "product_id"}),
        indexes = {
                // ✅ ESSENTIAL INDEXES ONLY for ProductFavorite

                // 1. Check if user favorited specific product (most common)
                @Index(name = "idx_product_favorites_user_product_deleted",
                        columnList = "user_id, product_id, is_deleted"),

                // 2. Get user's favorites (for favorites page)
                @Index(name = "idx_product_favorites_user_deleted_created",
                        columnList = "user_id, is_deleted, created_at"),

                // 3. Batch check favorites for multiple products
                @Index(name = "idx_product_favorites_user_deleted",
                        columnList = "user_id, is_deleted"),

                // 4. Base soft delete index
                @Index(name = "idx_product_favorites_deleted",
                        columnList = "is_deleted")
        })
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ProductFavorite extends BaseUUIDEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private Product product;

    public ProductFavorite(UUID userId, UUID productId) {
        this.userId = userId;
        this.productId = productId;
    }
}
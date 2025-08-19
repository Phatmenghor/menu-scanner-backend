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
           @Index(name = "idx_product_favorites_user_deleted", columnList = "user_id, is_deleted"),
           @Index(name = "idx_product_favorites_product_deleted", columnList = "product_id, is_deleted"),
           @Index(name = "idx_product_favorites_user_created_deleted", columnList = "user_id, created_at, is_deleted"),
           @Index(name = "idx_product_favorites_user_product_deleted", columnList = "user_id, product_id, is_deleted")
       })
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ProductFavorite extends BaseUUIDEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    public ProductFavorite(UUID userId, UUID productId) {
        this.userId = userId;
        this.productId = productId;
    }
}
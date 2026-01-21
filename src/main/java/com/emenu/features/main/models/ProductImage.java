package com.emenu.features.main.models;

import com.emenu.shared.domain.BaseUUIDEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "product_images")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ProductImage extends BaseUUIDEntity {

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private Product product;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    public ProductImage(UUID productId, String imageUrl) {
        this.productId = productId;
        this.imageUrl = imageUrl;
    }

    public ProductImage(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setProduct(Product product) {
        this.product = product;
        if (product != null) {
            this.productId = product.getId();
        } else {
            this.productId = null;
        }
    }
}
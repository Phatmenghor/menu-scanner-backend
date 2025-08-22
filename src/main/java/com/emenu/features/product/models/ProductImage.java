package com.emenu.features.product.models;

import com.emenu.enums.product.ImageType;
import com.emenu.shared.domain.BaseUUIDEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "product_images", indexes = {
        // ✅ ESSENTIAL INDEXES ONLY for ProductImage

        // 1. Loading images by product (most common query)
        @Index(name = "idx_product_images_product_deleted",
                columnList = "product_id, is_deleted"),

        // 2. Finding main images specifically (for getMainImageUrl)
        @Index(name = "idx_product_images_product_type_deleted",
                columnList = "product_id, image_type, is_deleted"),

        // 3. Base soft delete index
        @Index(name = "idx_product_images_deleted",
                columnList = "is_deleted")
})
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

    @Enumerated(EnumType.STRING)
    @Column(name = "image_type", nullable = false)
    private ImageType imageType = ImageType.GALLERY;

    public ProductImage(UUID productId, String imageUrl, ImageType imageType) {
        this.productId = productId;
        this.imageUrl = imageUrl;
        this.imageType = imageType;
    }

    public ProductImage(String imageUrl, ImageType imageType) {
        this.imageUrl = imageUrl;
        this.imageType = imageType;
    }

    public void setAsMain() {
        this.imageType = ImageType.MAIN;
    }

    public void setAsGallery() {
        this.imageType = ImageType.GALLERY;
    }

    public boolean isMain() {
        return ImageType.MAIN.equals(imageType);
    }

    public boolean isGallery() {
        return ImageType.GALLERY.equals(imageType);
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

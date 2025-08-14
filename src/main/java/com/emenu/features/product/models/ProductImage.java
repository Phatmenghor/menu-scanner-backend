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

    @Enumerated(EnumType.STRING)
    @Column(name = "image_type", nullable = false)
    private ImageType imageType = ImageType.GALLERY;

    // ================================
    // FIXED: Proper parent-child relationship management
    // ================================

    /**
     * FIXED: Set product with proper relationship management
     */
    public void setProduct(Product product) {
        this.product = product;
        if (product != null) {
            this.productId = product.getId();
        } else {
            this.productId = null;
        }
    }

    /**
     * FIXED: Set product ID with relationship sync
     */
    public void setProductId(UUID productId) {
        this.productId = productId;
        // Note: Don't set product here to avoid circular reference in collection management
    }

    // ================================
    // BUSINESS METHODS
    // ================================

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

    // ================================
    // CONSTRUCTORS
    // ================================

    public ProductImage(UUID productId, String imageUrl, ImageType imageType) {
        this.productId = productId;
        this.imageUrl = imageUrl;
        this.imageType = imageType;
    }

    public ProductImage(String imageUrl, ImageType imageType) {
        this.imageUrl = imageUrl;
        this.imageType = imageType;
    }

    // ================================
    // VALIDATION HELPERS
    // ================================

    public boolean hasValidUrl() {
        return imageUrl != null && !imageUrl.trim().isEmpty();
    }

    public boolean belongsToProduct(UUID productId) {
        return this.productId != null && this.productId.equals(productId);
    }

    @Override
    public String toString() {
        return "ProductImage{" +
                "id=" + getId() +
                ", productId=" + productId +
                ", imageUrl='" + imageUrl + '\'' +
                ", imageType=" + imageType +
                '}';
    }
}
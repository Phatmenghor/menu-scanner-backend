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

    // Business Methods
    public void setAsMain() {
        this.imageType = ImageType.MAIN;
    }

    public void setAsGallery() {
        this.imageType = ImageType.GALLERY;
    }

    public Boolean getIsMain() {
        return ImageType.MAIN.equals(imageType);
    }

    public Boolean getIsGallery() {
        return ImageType.GALLERY.equals(imageType);
    }
}
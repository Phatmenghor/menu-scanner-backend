package com.emenu.features.product.dto.update;

import com.emenu.enums.product.ImageType;
import lombok.Data;

import java.util.UUID;

@Data
public class ProductImageUpdateDto {

    private UUID id; // If provided, update existing; if null, create new

    private String imageUrl;

    private ImageType imageType = ImageType.MAIN;

    private Boolean toDelete = false; // Flag to mark for deletion

    // Helper methods
    public boolean isExisting() {
        return id != null;
    }

    public boolean isNew() {
        return id == null;
    }

    public boolean shouldDelete() {
        return Boolean.TRUE.equals(toDelete);
    }
}

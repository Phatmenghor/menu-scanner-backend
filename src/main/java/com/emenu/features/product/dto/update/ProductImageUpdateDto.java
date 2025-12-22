package com.emenu.features.product.dto.update;

import lombok.Data;

import java.util.UUID;

@Data
public class ProductImageUpdateDto {
    private UUID id;
    private String imageUrl;
    private Boolean toDelete = false;

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
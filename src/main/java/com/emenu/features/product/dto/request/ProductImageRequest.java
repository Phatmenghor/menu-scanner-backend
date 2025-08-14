package com.emenu.features.product.dto.request;

import lombok.Data;

import java.util.UUID;

@Data
public class ProductImageRequest {
    private UUID id; // For updates: if provided, update existing; if null, create new
    private String imageUrl; // Can be null (not required)
    private String imageType = "GALLERY"; // MAIN or GALLERY - auto-adjusted for single images
}

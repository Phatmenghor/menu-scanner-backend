package com.emenu.features.product.dto.request;

import com.emenu.enums.product.ImageType;
import lombok.Data;

import java.util.UUID;

@Data
public class ProductImageCreateDto {
    private UUID id; // For updates
    private String imageUrl;
    private ImageType imageType = ImageType.MAIN;
}

package com.emenu.features.product.dto.request;

import lombok.Data;

import java.util.UUID;

@Data
public class ProductImageCreateDto {
    private UUID id; // For updates
    private String imageUrl;
    private String imageType = "GALLERY";
}

package com.emenu.features.product.dto.response;

import lombok.Data;

import java.util.UUID;

@Data
public class ProductImageDto {
    private UUID id;
    private String imageUrl;
    private String imageType;
}
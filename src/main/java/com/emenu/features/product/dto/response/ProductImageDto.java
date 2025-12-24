package com.emenu.features.product.dto.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ProductImageDto {
    private UUID id;
    private String imageUrl;
    private LocalDateTime createdAt;
}
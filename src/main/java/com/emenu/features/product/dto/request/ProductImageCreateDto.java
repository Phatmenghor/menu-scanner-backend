package com.emenu.features.product.dto.request;

import lombok.Data;

import java.util.UUID;

@Data
public class ProductImageCreateDto {
    private UUID id;
    private String imageUrl;
}
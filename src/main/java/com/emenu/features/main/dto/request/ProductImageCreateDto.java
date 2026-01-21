package com.emenu.features.main.dto.request;

import lombok.Data;

import java.util.UUID;

@Data
public class ProductImageCreateDto {
    private UUID id;
    private String imageUrl;
}
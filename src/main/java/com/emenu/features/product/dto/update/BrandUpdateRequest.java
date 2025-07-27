package com.emenu.features.product.dto.update;

import com.emenu.enums.common.Status;
import lombok.Data;

@Data
public class BrandUpdateRequest {
    private String name;
    private String imageUrl;
    private String description;
    private Status status;
}
package com.emenu.features.business.dto.update;

import com.emenu.enums.common.Status;
import lombok.Data;

@Data
public class CategoryUpdateRequest {
    private String name;
    private String imageUrl;
    private Status status;
}
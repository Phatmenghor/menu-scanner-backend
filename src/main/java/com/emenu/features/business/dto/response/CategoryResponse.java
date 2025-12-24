package com.emenu.features.business.dto.response;

import com.emenu.enums.common.Status;
import com.emenu.shared.dto.BaseAuditResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class CategoryResponse extends BaseAuditResponse {
    private UUID businessId;
    private String businessName;
    private String name;
    private String imageUrl;
    private Status status;

    private Long activeProducts;
    private Long totalProducts; // Number of products in this category
}

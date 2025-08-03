package com.emenu.features.business.dto.response;

import com.emenu.enums.common.Status;
import com.emenu.shared.dto.BaseAuditResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class BrandResponse extends BaseAuditResponse {
    private UUID businessId;
    private String businessName;
    private String name;
    private String imageUrl;
    private String description;
    private Status status;
    
    // Statistics
    private Long totalProducts; // Number of products using this brand
    private Long activeProducts; // Number of active products using this brand
}
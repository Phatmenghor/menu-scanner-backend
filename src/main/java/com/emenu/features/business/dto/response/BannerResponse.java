package com.emenu.features.business.dto.response;

import com.emenu.shared.dto.BaseAuditResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class BannerResponse extends BaseAuditResponse {
    private UUID businessId;
    private String businessName;
    private String title;
    private String description;
    private String imageUrl;
    private String linkUrl;
    private Boolean isActive;
    private Integer displayOrder;
    private String bannerType;
}
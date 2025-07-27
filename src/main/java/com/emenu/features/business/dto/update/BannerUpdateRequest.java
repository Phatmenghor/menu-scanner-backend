package com.emenu.features.business.dto.update;

import lombok.Data;

@Data
public class BannerUpdateRequest {
    private String title;
    private String description;
    private String imageUrl;
    private String linkUrl;
    private Boolean isActive;
    private Integer displayOrder;
    private String bannerType;
}
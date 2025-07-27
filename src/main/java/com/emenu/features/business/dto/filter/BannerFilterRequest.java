package com.emenu.features.business.dto.filter;

import com.emenu.shared.dto.BaseFilterRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class BannerFilterRequest extends BaseFilterRequest {
    private UUID businessId;
    private Boolean isActive;
    private String bannerType;
}
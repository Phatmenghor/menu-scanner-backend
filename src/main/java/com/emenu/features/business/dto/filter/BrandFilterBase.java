package com.emenu.features.business.dto.filter;

import com.emenu.enums.common.Status;

import java.util.UUID;

public interface BrandFilterBase {
    UUID getBusinessId();
    Status getStatus();
    String getSearch();
    String getSortBy();
    String getSortDirection();
}

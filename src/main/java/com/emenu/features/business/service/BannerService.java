package com.emenu.features.business.service;

import com.emenu.features.business.dto.filter.BannerFilterRequest;
import com.emenu.features.business.dto.request.BannerCreateRequest;
import com.emenu.features.business.dto.response.BannerResponse;
import com.emenu.features.business.dto.update.BannerUpdateRequest;
import com.emenu.shared.dto.PaginationResponse;

import java.util.List;
import java.util.UUID;

public interface BannerService {
    
    // CRUD Operations
    BannerResponse createBanner(BannerCreateRequest request);
    PaginationResponse<BannerResponse> getAllBanners(BannerFilterRequest filter);
    BannerResponse getBannerById(UUID id);
    BannerResponse updateBanner(UUID id, BannerUpdateRequest request);
    BannerResponse deleteBanner(UUID id);
}
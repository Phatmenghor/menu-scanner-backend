package com.emenu.features.business.service;

import com.emenu.features.business.dto.filter.BrandFilterRequest;
import com.emenu.features.business.dto.request.BrandCreateRequest;
import com.emenu.features.business.dto.response.BrandResponse;
import com.emenu.features.business.dto.update.BrandUpdateRequest;
import com.emenu.shared.dto.PaginationResponse;

import java.util.UUID;

public interface BrandService {
    BrandResponse createBrand(BrandCreateRequest request);
    PaginationResponse<BrandResponse> getAllBrands(BrandFilterRequest filter);
    BrandResponse getBrandById(UUID id);
    BrandResponse updateBrand(UUID id, BrandUpdateRequest request);
    BrandResponse deleteBrand(UUID id);
}

package com.emenu.features.business.service;

import com.emenu.features.business.dto.filter.BrandFilterRequest;
import com.emenu.features.business.dto.filter.BrandAllFilterRequest;
import com.emenu.features.business.dto.request.BrandCreateRequest;
import com.emenu.features.business.dto.response.BrandResponse;
import com.emenu.features.business.dto.update.BrandUpdateRequest;
import com.emenu.shared.dto.PaginationResponse;

import java.util.List;
import java.util.UUID;

public interface BrandService {
    BrandResponse createBrand(BrandCreateRequest request);
    PaginationResponse<BrandResponse> getAllBrands(BrandFilterRequest filter);
    List<BrandResponse> getAllListBrands(BrandAllFilterRequest filter);
    BrandResponse getBrandById(UUID id);
    BrandResponse updateBrand(UUID id, BrandUpdateRequest request);
    BrandResponse deleteBrand(UUID id);
}

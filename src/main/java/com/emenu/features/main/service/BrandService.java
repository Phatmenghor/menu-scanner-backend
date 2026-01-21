package com.emenu.features.main.service;

import com.emenu.features.main.dto.filter.BrandFilterRequest;
import com.emenu.features.main.dto.filter.BrandAllFilterRequest;
import com.emenu.features.main.dto.request.BrandCreateRequest;
import com.emenu.features.main.dto.response.BrandResponse;
import com.emenu.features.main.dto.update.BrandUpdateRequest;
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

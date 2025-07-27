package com.emenu.features.product.service;

import com.emenu.features.product.dto.filter.BrandFilterRequest;
import com.emenu.features.product.dto.request.BrandCreateRequest;
import com.emenu.features.product.dto.response.BrandResponse;
import com.emenu.features.product.dto.update.BrandUpdateRequest;
import com.emenu.shared.dto.PaginationResponse;

import java.util.List;
import java.util.UUID;

public interface BrandService {
    
    // CRUD Operations
    BrandResponse createBrand(BrandCreateRequest request);
    PaginationResponse<BrandResponse> getAllBrands(BrandFilterRequest filter);
    BrandResponse getBrandById(UUID id);
    BrandResponse updateBrand(UUID id, BrandUpdateRequest request);
    BrandResponse deleteBrand(UUID id);
    
    // Additional Operations
    List<BrandResponse> getActiveBrandsByBusiness(UUID businessId);
}

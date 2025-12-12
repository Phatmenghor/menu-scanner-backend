package com.emenu.features.location.service;

import com.emenu.features.location.dto.filter.DistrictFilterRequest;
import com.emenu.features.location.dto.request.DistrictRequest;
import com.emenu.features.location.dto.response.DistrictResponse;
import com.emenu.shared.dto.PaginationResponse;

import java.util.List;
import java.util.UUID;

public interface DistrictService {
    DistrictResponse createDistrict(DistrictRequest request);
    PaginationResponse<DistrictResponse> getAllDistricts(DistrictFilterRequest request);
    DistrictResponse getDistrictById(UUID id);
    DistrictResponse getDistrictByCode(String code);
    DistrictResponse getDistrictByNameEn(String nameEn);
    DistrictResponse getDistrictByNameKh(String nameKh);
    DistrictResponse updateDistrict(UUID id, DistrictRequest request);
    void deleteDistrict(UUID id);
    List<DistrictResponse> getDistrictsByProvinceCode(String provinceCode);
}
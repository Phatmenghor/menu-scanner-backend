package com.emenu.features.location.service;

import com.emenu.features.location.dto.filter.ProvinceFilterRequest;
import com.emenu.features.location.dto.request.ProvinceRequest;
import com.emenu.features.location.dto.response.ProvinceResponse;
import com.emenu.shared.dto.PaginationResponse;

import java.util.List;
import java.util.UUID;

public interface ProvinceService {
    ProvinceResponse createProvince(ProvinceRequest request);
    PaginationResponse<ProvinceResponse> getAllProvinces(ProvinceFilterRequest request);
    ProvinceResponse getProvinceById(UUID id);
    ProvinceResponse getProvinceByCode(String code);
    ProvinceResponse getProvinceByNameEn(String nameEn);
    ProvinceResponse getProvinceByNameKh(String nameKh);
    ProvinceResponse updateProvince(UUID id, ProvinceRequest request);
    void deleteProvince(UUID id);
}
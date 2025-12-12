package com.emenu.features.location.service;

import com.emenu.features.location.dto.filter.VillageFilterRequest;
import com.emenu.features.location.dto.request.VillageRequest;
import com.emenu.features.location.dto.response.VillageResponse;
import com.emenu.shared.dto.PaginationResponse;

import java.util.List;
import java.util.UUID;

public interface VillageService {
    VillageResponse createVillage(VillageRequest request);
    PaginationResponse<VillageResponse> getAllVillages(VillageFilterRequest request);
    VillageResponse getVillageById(UUID id);
    VillageResponse getVillageByCode(String code);
    VillageResponse getVillageByNameEn(String nameEn);
    VillageResponse getVillageByNameKh(String nameKh);
    VillageResponse updateVillage(UUID id, VillageRequest request);
    void deleteVillage(UUID id);
    List<VillageResponse> getVillagesByCommuneCode(String communeCode);
}
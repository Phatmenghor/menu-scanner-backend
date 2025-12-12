package com.emenu.features.location.service;

import com.emenu.features.location.dto.filter.CommuneFilterRequest;
import com.emenu.features.location.dto.request.CommuneRequest;
import com.emenu.features.location.dto.response.CommuneResponse;
import com.emenu.shared.dto.PaginationResponse;

import java.util.List;
import java.util.UUID;

public interface CommuneService {
    CommuneResponse createCommune(CommuneRequest request);
    PaginationResponse<CommuneResponse> getAllCommunes(CommuneFilterRequest request);
    CommuneResponse getCommuneById(UUID id);
    CommuneResponse getCommuneByCode(String code);
    CommuneResponse getCommuneByNameEn(String nameEn);
    CommuneResponse getCommuneByNameKh(String nameKh);
    CommuneResponse updateCommune(UUID id, CommuneRequest request);
    void deleteCommune(UUID id);
}
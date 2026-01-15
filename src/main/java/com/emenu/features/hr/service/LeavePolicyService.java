package com.emenu.features.hr.service;

import com.emenu.features.hr.dto.filter.LeavePolicyFilterRequest;
import com.emenu.features.hr.dto.request.LeavePolicyCreateRequest;
import com.emenu.features.hr.dto.response.LeavePolicyResponse;
import com.emenu.features.hr.dto.update.LeavePolicyUpdateRequest;
import com.emenu.shared.dto.PaginationResponse;

import java.util.List;
import java.util.UUID;

public interface LeavePolicyService {
    LeavePolicyResponse create(LeavePolicyCreateRequest request);
    LeavePolicyResponse getById(UUID id);
    PaginationResponse<LeavePolicyResponse> getAll(LeavePolicyFilterRequest filter);
    List<LeavePolicyResponse> getByBusinessId(UUID businessId);
    LeavePolicyResponse update(UUID id, LeavePolicyUpdateRequest request);
    void delete(UUID id);
}
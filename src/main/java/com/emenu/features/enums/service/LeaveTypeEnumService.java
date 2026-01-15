package com.emenu.features.enums.service;

import com.emenu.features.enums.dto.filter.ConfigEnumFilterRequest;
import com.emenu.features.enums.dto.request.LeaveTypeEnumCreateRequest;
import com.emenu.features.enums.dto.response.LeaveTypeEnumResponse;
import com.emenu.features.enums.dto.update.LeaveTypeEnumUpdateRequest;
import com.emenu.shared.dto.PaginationResponse;

import java.util.List;
import java.util.UUID;

public interface LeaveTypeEnumService {
    LeaveTypeEnumResponse create(LeaveTypeEnumCreateRequest request);
    LeaveTypeEnumResponse getById(UUID id);
    PaginationResponse<LeaveTypeEnumResponse> getAll(ConfigEnumFilterRequest filter);
    List<LeaveTypeEnumResponse> getByBusinessId(UUID businessId);
    LeaveTypeEnumResponse update(UUID id, LeaveTypeEnumUpdateRequest request);
    void delete(UUID id);
}
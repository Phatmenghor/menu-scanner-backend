package com.emenu.features.setting.service;

import com.emenu.features.setting.dto.filter.ConfigEnumFilterRequest;
import com.emenu.features.setting.dto.request.LeaveTypeEnumCreateRequest;
import com.emenu.features.setting.dto.response.LeaveTypeEnumResponse;
import com.emenu.features.setting.dto.update.LeaveTypeEnumUpdateRequest;
import com.emenu.shared.dto.PaginationResponse;

import java.util.List;
import java.util.UUID;

public interface LeaveTypeEnumService {
    LeaveTypeEnumResponse create(LeaveTypeEnumCreateRequest request);
    LeaveTypeEnumResponse getById(UUID id);
    PaginationResponse<LeaveTypeEnumResponse> getAll(ConfigEnumFilterRequest filter);
    List<LeaveTypeEnumResponse> getAllList(ConfigEnumFilterRequest filter);
    List<LeaveTypeEnumResponse> getByBusinessId(UUID businessId);
    LeaveTypeEnumResponse update(UUID id, LeaveTypeEnumUpdateRequest request);
    LeaveTypeEnumResponse delete(UUID id);
}
package com.emenu.features.enums.service;

import com.emenu.features.enums.dto.filter.ConfigEnumFilterRequest;
import com.emenu.features.enums.dto.request.LeaveStatusEnumCreateRequest;
import com.emenu.features.enums.dto.response.LeaveStatusEnumResponse;
import com.emenu.features.enums.dto.response.WorkScheduleTypeEnumResponse;
import com.emenu.features.enums.dto.update.LeaveStatusEnumUpdateRequest;
import com.emenu.shared.dto.PaginationResponse;

import java.util.List;
import java.util.UUID;

public interface LeaveStatusEnumService {
    LeaveStatusEnumResponse create(LeaveStatusEnumCreateRequest request);
    LeaveStatusEnumResponse getById(UUID id);
    PaginationResponse<LeaveStatusEnumResponse> getAll(ConfigEnumFilterRequest filter);
    List<LeaveStatusEnumResponse> getByBusinessId(UUID businessId);
    LeaveStatusEnumResponse update(UUID id, LeaveStatusEnumUpdateRequest request);
    LeaveStatusEnumResponse delete(UUID id);
}
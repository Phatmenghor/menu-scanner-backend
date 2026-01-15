package com.emenu.features.enums.service;

import com.emenu.features.enums.dto.filter.ConfigEnumFilterRequest;
import com.emenu.features.enums.dto.request.WorkScheduleTypeEnumCreateRequest;
import com.emenu.features.enums.dto.response.WorkScheduleTypeEnumResponse;
import com.emenu.features.enums.dto.update.WorkScheduleTypeEnumUpdateRequest;
import com.emenu.shared.dto.PaginationResponse;

import java.util.List;
import java.util.UUID;

public interface WorkScheduleTypeEnumService {
    WorkScheduleTypeEnumResponse create(WorkScheduleTypeEnumCreateRequest request);
    WorkScheduleTypeEnumResponse getById(UUID id);
    PaginationResponse<WorkScheduleTypeEnumResponse> getAll(ConfigEnumFilterRequest filter);
    List<WorkScheduleTypeEnumResponse> getByBusinessId(UUID businessId);
    WorkScheduleTypeEnumResponse update(UUID id, WorkScheduleTypeEnumUpdateRequest request);
    void delete(UUID id);
}
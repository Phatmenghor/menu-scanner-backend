package com.emenu.features.setting.service;

import com.emenu.features.setting.dto.filter.ConfigEnumFilterRequest;
import com.emenu.features.setting.dto.request.WorkScheduleTypeEnumCreateRequest;
import com.emenu.features.setting.dto.response.WorkScheduleTypeEnumResponse;
import com.emenu.features.setting.dto.update.WorkScheduleTypeEnumUpdateRequest;
import com.emenu.shared.dto.PaginationResponse;

import java.util.List;
import java.util.UUID;

public interface WorkScheduleTypeEnumService {
    WorkScheduleTypeEnumResponse create(WorkScheduleTypeEnumCreateRequest request);
    WorkScheduleTypeEnumResponse getById(UUID id);
    PaginationResponse<WorkScheduleTypeEnumResponse> getAll(ConfigEnumFilterRequest filter);
    List<WorkScheduleTypeEnumResponse> getAllList(ConfigEnumFilterRequest filter);
    List<WorkScheduleTypeEnumResponse> getByBusinessId(UUID businessId);
    WorkScheduleTypeEnumResponse update(UUID id, WorkScheduleTypeEnumUpdateRequest request);
    WorkScheduleTypeEnumResponse delete(UUID id);
}
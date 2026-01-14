package com.emenu.features.hr.service;

import com.emenu.features.hr.dto.filter.WorkScheduleFilterRequest;
import com.emenu.features.hr.dto.request.WorkScheduleCreateRequest;
import com.emenu.features.hr.dto.response.WorkScheduleResponse;
import com.emenu.features.hr.dto.update.WorkScheduleUpdateRequest;
import com.emenu.shared.dto.PaginationResponse;

import java.util.List;
import java.util.UUID;

public interface WorkScheduleService {
    WorkScheduleResponse create(WorkScheduleCreateRequest request);
    WorkScheduleResponse getById(UUID id);
    PaginationResponse<WorkScheduleResponse> getAll(WorkScheduleFilterRequest filter);
    List<WorkScheduleResponse> getByUserId(UUID userId);
    WorkScheduleResponse update(UUID id, WorkScheduleUpdateRequest request);
    void delete(UUID id);
}
package com.emenu.features.enums.service;

import com.emenu.features.enums.dto.filter.ConfigEnumFilterRequest;
import com.emenu.features.enums.dto.request.AttendanceStatusEnumCreateRequest;
import com.emenu.features.enums.dto.response.AttendanceStatusEnumResponse;
import com.emenu.features.enums.dto.response.WorkScheduleTypeEnumResponse;
import com.emenu.features.enums.dto.update.AttendanceStatusEnumUpdateRequest;
import com.emenu.shared.dto.PaginationResponse;

import java.util.List;
import java.util.UUID;

public interface AttendanceStatusEnumService {
    AttendanceStatusEnumResponse create(AttendanceStatusEnumCreateRequest request);
    AttendanceStatusEnumResponse getById(UUID id);
    PaginationResponse<AttendanceStatusEnumResponse> getAll(ConfigEnumFilterRequest filter);
    List<AttendanceStatusEnumResponse> getByBusinessId(UUID businessId);
    AttendanceStatusEnumResponse update(UUID id, AttendanceStatusEnumUpdateRequest request);
    AttendanceStatusEnumResponse delete(UUID id);
}
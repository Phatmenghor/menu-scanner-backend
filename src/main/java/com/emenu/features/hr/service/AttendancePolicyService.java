package com.emenu.features.hr.service;

import com.emenu.features.hr.dto.filter.AttendancePolicyFilterRequest;
import com.emenu.features.hr.dto.request.AttendancePolicyCreateRequest;
import com.emenu.features.hr.dto.response.AttendancePolicyResponse;
import com.emenu.features.hr.dto.update.AttendancePolicyUpdateRequest;
import com.emenu.shared.dto.PaginationResponse;

import java.util.List;
import java.util.UUID;

public interface AttendancePolicyService {
    AttendancePolicyResponse create(AttendancePolicyCreateRequest request);
    AttendancePolicyResponse getById(UUID id);
    PaginationResponse<AttendancePolicyResponse> getAll(AttendancePolicyFilterRequest filter);
    List<AttendancePolicyResponse> getByBusinessId(UUID businessId);
    AttendancePolicyResponse update(UUID id, AttendancePolicyUpdateRequest request);
    AttendancePolicyResponse delete(UUID id);
}
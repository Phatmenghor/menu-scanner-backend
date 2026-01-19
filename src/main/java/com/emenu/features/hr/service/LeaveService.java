package com.emenu.features.hr.service;

import com.emenu.features.hr.dto.filter.LeaveFilterRequest;
import com.emenu.features.hr.dto.request.LeaveApprovalRequest;
import com.emenu.features.hr.dto.request.LeaveCreateRequest;
import com.emenu.features.hr.dto.response.LeaveResponse;
import com.emenu.features.hr.dto.update.LeaveUpdateRequest;
import com.emenu.shared.dto.PaginationResponse;

import java.util.UUID;

public interface LeaveService {
    LeaveResponse create(LeaveCreateRequest request, UUID userId, UUID businessId);
    LeaveResponse getById(UUID id);
    PaginationResponse<LeaveResponse> getAll(LeaveFilterRequest filter);
    LeaveResponse update(UUID id, LeaveUpdateRequest request);
    LeaveResponse approve(UUID id, LeaveApprovalRequest request, UUID actionBy);
    LeaveResponse delete(UUID id);
}
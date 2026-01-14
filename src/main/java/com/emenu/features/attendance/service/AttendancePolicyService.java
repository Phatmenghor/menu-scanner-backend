package com.emenu.features.attendance.service;

import com.emenu.features.attendance.dto.request.AttendancePolicyCreateRequest;
import com.emenu.features.attendance.dto.response.AttendancePolicyResponse;
import com.emenu.features.attendance.dto.update.AttendancePolicyUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AttendancePolicyService {

    AttendancePolicyResponse createPolicy(AttendancePolicyCreateRequest request);

    AttendancePolicyResponse updatePolicy(Long id, AttendancePolicyUpdateRequest request);

    AttendancePolicyResponse getPolicyById(Long id);

    List<AttendancePolicyResponse> getPoliciesByBusinessId(Long businessId);

    Page<AttendancePolicyResponse> getAllPolicies(Pageable pageable);

    void deletePolicy(Long id);
}

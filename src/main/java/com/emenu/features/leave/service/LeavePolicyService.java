package com.emenu.features.leave.service;

import com.emenu.features.leave.dto.request.LeavePolicyCreateRequest;
import com.emenu.features.leave.dto.response.LeavePolicyResponse;
import com.emenu.features.leave.dto.update.LeavePolicyUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface LeavePolicyService {

    LeavePolicyResponse createPolicy(LeavePolicyCreateRequest request);

    LeavePolicyResponse updatePolicy(Long id, LeavePolicyUpdateRequest request);

    LeavePolicyResponse getPolicyById(Long id);

    List<LeavePolicyResponse> getPoliciesByBusinessId(Long businessId);

    Page<LeavePolicyResponse> getAllPolicies(Pageable pageable);

    void deletePolicy(Long id);
}

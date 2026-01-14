package com.emenu.features.leave.service.impl;

import com.emenu.exception.ResourceNotFoundException;
import com.emenu.features.auth.models.Business;
import com.emenu.features.auth.repository.BusinessRepository;
import com.emenu.features.leave.dto.request.LeavePolicyCreateRequest;
import com.emenu.features.leave.dto.response.LeavePolicyResponse;
import com.emenu.features.leave.dto.update.LeavePolicyUpdateRequest;
import com.emenu.features.leave.mapper.LeavePolicyMapper;
import com.emenu.features.leave.models.LeavePolicy;
import com.emenu.features.leave.repository.LeavePolicyRepository;
import com.emenu.features.leave.service.LeavePolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeavePolicyServiceImpl implements LeavePolicyService {

    private final LeavePolicyRepository policyRepository;
    private final BusinessRepository businessRepository;
    private final LeavePolicyMapper policyMapper;

    @Override
    @Transactional
    public LeavePolicyResponse createPolicy(LeavePolicyCreateRequest request) {
        Business business = businessRepository.findById(request.getBusinessId())
                .orElseThrow(() -> new ResourceNotFoundException("Business not found with id: " + request.getBusinessId()));

        LeavePolicy policy = policyMapper.toEntity(request, business);
        LeavePolicy savedPolicy = policyRepository.save(policy);

        return policyMapper.toResponse(savedPolicy);
    }

    @Override
    @Transactional
    public LeavePolicyResponse updatePolicy(Long id, LeavePolicyUpdateRequest request) {
        LeavePolicy policy = policyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave policy not found with id: " + id));

        if (request.getPolicyName() != null) policy.setPolicyName(request.getPolicyName());
        if (request.getLeaveType() != null) policy.setLeaveType(request.getLeaveType());
        if (request.getAnnualAllowance() != null) policy.setAnnualAllowance(request.getAnnualAllowance());
        if (request.getAllowHalfDay() != null) policy.setAllowHalfDay(request.getAllowHalfDay());
        if (request.getRequiresApproval() != null) policy.setRequiresApproval(request.getRequiresApproval());
        if (request.getMinAdvanceNoticeDays() != null) policy.setMinAdvanceNoticeDays(request.getMinAdvanceNoticeDays());
        if (request.getMaxConsecutiveDays() != null) policy.setMaxConsecutiveDays(request.getMaxConsecutiveDays());
        if (request.getDescription() != null) policy.setDescription(request.getDescription());
        if (request.getIsActive() != null) policy.setIsActive(request.getIsActive());

        LeavePolicy updatedPolicy = policyRepository.save(policy);
        return policyMapper.toResponse(updatedPolicy);
    }

    @Override
    @Transactional(readOnly = true)
    public LeavePolicyResponse getPolicyById(Long id) {
        LeavePolicy policy = policyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave policy not found with id: " + id));

        return policyMapper.toResponse(policy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeavePolicyResponse> getPoliciesByBusinessId(Long businessId) {
        return policyRepository.findByBusinessId(businessId).stream()
                .map(policyMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LeavePolicyResponse> getAllPolicies(Pageable pageable) {
        return policyRepository.findAll(pageable)
                .map(policyMapper::toResponse);
    }

    @Override
    @Transactional
    public void deletePolicy(Long id) {
        if (!policyRepository.existsById(id)) {
            throw new ResourceNotFoundException("Leave policy not found with id: " + id);
        }
        policyRepository.deleteById(id);
    }
}

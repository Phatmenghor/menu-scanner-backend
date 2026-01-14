package com.emenu.features.attendance.service.impl;

import com.emenu.exception.ResourceNotFoundException;
import com.emenu.features.attendance.dto.request.AttendancePolicyCreateRequest;
import com.emenu.features.attendance.dto.response.AttendancePolicyResponse;
import com.emenu.features.attendance.dto.update.AttendancePolicyUpdateRequest;
import com.emenu.features.attendance.mapper.AttendancePolicyMapper;
import com.emenu.features.attendance.models.AttendancePolicy;
import com.emenu.features.attendance.repository.AttendancePolicyRepository;
import com.emenu.features.attendance.service.AttendancePolicyService;
import com.emenu.features.auth.models.Business;
import com.emenu.features.auth.repository.BusinessRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendancePolicyServiceImpl implements AttendancePolicyService {

    private final AttendancePolicyRepository policyRepository;
    private final BusinessRepository businessRepository;
    private final AttendancePolicyMapper policyMapper;

    @Override
    @Transactional
    public AttendancePolicyResponse createPolicy(AttendancePolicyCreateRequest request) {
        Business business = businessRepository.findById(request.getBusinessId())
                .orElseThrow(() -> new ResourceNotFoundException("Business not found with id: " + request.getBusinessId()));

        AttendancePolicy policy = policyMapper.toEntity(request, business);
        AttendancePolicy savedPolicy = policyRepository.save(policy);

        return policyMapper.toResponse(savedPolicy);
    }

    @Override
    @Transactional
    public AttendancePolicyResponse updatePolicy(Long id, AttendancePolicyUpdateRequest request) {
        AttendancePolicy policy = policyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance policy not found with id: " + id));

        if (request.getPolicyName() != null) policy.setPolicyName(request.getPolicyName());
        if (request.getDescription() != null) policy.setDescription(request.getDescription());
        if (request.getStartTime() != null) policy.setStartTime(request.getStartTime());
        if (request.getEndTime() != null) policy.setEndTime(request.getEndTime());
        if (request.getLateThresholdMinutes() != null) policy.setLateThresholdMinutes(request.getLateThresholdMinutes());
        if (request.getHalfDayThresholdMinutes() != null) policy.setHalfDayThresholdMinutes(request.getHalfDayThresholdMinutes());
        if (request.getBreakStartTime() != null) policy.setBreakStartTime(request.getBreakStartTime());
        if (request.getBreakEndTime() != null) policy.setBreakEndTime(request.getBreakEndTime());
        if (request.getRequireLocationCheck() != null) policy.setRequireLocationCheck(request.getRequireLocationCheck());
        if (request.getOfficeLatitude() != null) policy.setOfficeLatitude(request.getOfficeLatitude());
        if (request.getOfficeLongitude() != null) policy.setOfficeLongitude(request.getOfficeLongitude());
        if (request.getAllowedRadiusMeters() != null) policy.setAllowedRadiusMeters(request.getAllowedRadiusMeters());
        if (request.getIsActive() != null) policy.setIsActive(request.getIsActive());

        AttendancePolicy updatedPolicy = policyRepository.save(policy);
        return policyMapper.toResponse(updatedPolicy);
    }

    @Override
    @Transactional(readOnly = true)
    public AttendancePolicyResponse getPolicyById(Long id) {
        AttendancePolicy policy = policyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance policy not found with id: " + id));

        return policyMapper.toResponse(policy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendancePolicyResponse> getPoliciesByBusinessId(Long businessId) {
        return policyRepository.findByBusinessId(businessId).stream()
                .map(policyMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AttendancePolicyResponse> getAllPolicies(Pageable pageable) {
        return policyRepository.findAll(pageable)
                .map(policyMapper::toResponse);
    }

    @Override
    @Transactional
    public void deletePolicy(Long id) {
        if (!policyRepository.existsById(id)) {
            throw new ResourceNotFoundException("Attendance policy not found with id: " + id);
        }
        policyRepository.deleteById(id);
    }
}

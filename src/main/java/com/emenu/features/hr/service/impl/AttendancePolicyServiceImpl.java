package com.emenu.features.hr.service.impl;

import com.emenu.features.hr.dto.filter.AttendancePolicyFilterRequest;
import com.emenu.features.hr.dto.request.AttendancePolicyCreateRequest;
import com.emenu.features.hr.dto.response.AttendancePolicyResponse;
import com.emenu.features.hr.dto.update.AttendancePolicyUpdateRequest;
import com.emenu.features.hr.mapper.AttendancePolicyMapper;
import com.emenu.features.hr.models.AttendancePolicy;
import com.emenu.features.hr.repository.AttendancePolicyRepository;
import com.emenu.features.hr.service.AttendancePolicyService;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.mapper.PaginationMapper;
import com.emenu.shared.pagination.PaginationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AttendancePolicyServiceImpl implements AttendancePolicyService {
    
    private final AttendancePolicyRepository repository;
    private final AttendancePolicyMapper mapper;
    private final PaginationMapper paginationMapper;
    
    @Override
    public AttendancePolicyResponse create(AttendancePolicyCreateRequest request) {
        log.info("Creating attendance policy: {}", request.getPolicyName());
        AttendancePolicy policy = mapper.toEntity(request);
        policy = repository.save(policy);
        return mapper.toResponse(policy);
    }
    
    @Override
    @Transactional(readOnly = true)
    public AttendancePolicyResponse getById(UUID id) {
        AttendancePolicy policy = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance policy not found"));
        return mapper.toResponse(policy);
    }
    
    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<AttendancePolicyResponse> getAll(AttendancePolicyFilterRequest filter) {
        Pageable pageable = PaginationUtils.createPageable(
                filter.getPageNo(), 
                filter.getPageSize(), 
                filter.getSortBy(), 
                filter.getSortDirection()
        );
        
        Page<AttendancePolicy> page = repository.findWithFilters(
                filter.getBusinessId(),
                filter.getSearch(),
                pageable
        );
        
        return paginationMapper.toPaginationResponse(page,
                policies -> policies.stream().map(mapper::toResponse).toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<AttendancePolicyResponse> getByBusinessId(UUID businessId) {
        List<AttendancePolicy> policies = repository.findByBusinessIdAndIsDeletedFalse(businessId);
        return mapper.toResponseList(policies);
    }
    
    @Override
    public AttendancePolicyResponse update(UUID id, AttendancePolicyUpdateRequest request) {
        AttendancePolicy policy = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance policy not found"));
        mapper.updateEntity(request, policy);
        policy = repository.save(policy);
        return mapper.toResponse(policy);
    }
    
    @Override
    public void delete(UUID id) {
        AttendancePolicy policy = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance policy not found"));
        policy.softDelete();
        repository.save(policy);
    }
}
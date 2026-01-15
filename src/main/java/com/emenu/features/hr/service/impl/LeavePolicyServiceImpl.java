
package com.emenu.features.hr.service.impl;

import com.emenu.exception.custom.ResourceNotFoundException;
import com.emenu.features.enums.models.LeaveTypeEnum;
import com.emenu.features.enums.repository.LeaveTypeEnumRepository;
import com.emenu.features.hr.dto.filter.LeavePolicyFilterRequest;
import com.emenu.features.hr.dto.request.LeavePolicyCreateRequest;
import com.emenu.features.hr.dto.response.LeavePolicyResponse;
import com.emenu.features.hr.dto.update.LeavePolicyUpdateRequest;
import com.emenu.features.hr.mapper.LeavePolicyMapper;
import com.emenu.features.hr.models.LeavePolicy;
import com.emenu.features.hr.repository.LeavePolicyRepository;
import com.emenu.features.hr.service.LeavePolicyService;
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
public class LeavePolicyServiceImpl implements LeavePolicyService {

    private final LeavePolicyRepository repository;
    private final LeaveTypeEnumRepository typeEnumRepository;
    private final LeavePolicyMapper mapper;
    private final PaginationMapper paginationMapper;

    @Override
    public LeavePolicyResponse create(LeavePolicyCreateRequest request) {
        log.info("Creating leave policy: {}", request.getPolicyName());

        final LeavePolicy policy = mapper.toEntity(request);

        // Resolve type enum if provided
        if (request.getTypeEnumName() != null) {
            final UUID businessId = request.getBusinessId();
            final String typeEnumName = request.getTypeEnumName();

            LeaveTypeEnum typeEnum = typeEnumRepository
                    .findByBusinessIdAndEnumNameAndIsDeletedFalse(businessId, typeEnumName)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Leave type enum not found: " + typeEnumName));
            policy.setTypeEnumId(typeEnum.getId());
        }

        LeavePolicy savedPolicy = repository.save(policy);
        log.info("Leave policy created: {}", savedPolicy.getId());
        return enrichResponse(mapper.toResponse(savedPolicy));
    }

    @Override
    @Transactional(readOnly = true)
    public LeavePolicyResponse getById(UUID id) {
        LeavePolicy policy = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave policy not found"));
        return enrichResponse(mapper.toResponse(policy));
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<LeavePolicyResponse> getAll(LeavePolicyFilterRequest filter) {
        Pageable pageable = PaginationUtils.createPageable(
                filter.getPageNo(),
                filter.getPageSize(),
                filter.getSortBy(),
                filter.getSortDirection()
        );

        Page<LeavePolicy> page = repository.findWithFilters(
                filter.getBusinessId(),
                filter.getSearch(),
                pageable
        );

        return paginationMapper.toPaginationResponse(page,
                policies -> policies.stream()
                        .map(mapper::toResponse)
                        .map(this::enrichResponse)
                        .toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeavePolicyResponse> getByBusinessId(UUID businessId) {
        List<LeavePolicy> policies = repository.findByBusinessIdAndIsDeletedFalse(businessId);
        return policies.stream()
                .map(mapper::toResponse)
                .map(this::enrichResponse)
                .toList();
    }

    @Override
    public LeavePolicyResponse update(UUID id, LeavePolicyUpdateRequest request) {
        log.info("Updating leave policy: {}", id);

        final LeavePolicy policy = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave policy not found"));

        // Resolve type enum if provided
        if (request.getTypeEnumName() != null) {
            final UUID businessId = policy.getBusinessId();
            final String typeEnumName = request.getTypeEnumName();

            LeaveTypeEnum typeEnum = typeEnumRepository
                    .findByBusinessIdAndEnumNameAndIsDeletedFalse(businessId, typeEnumName)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Leave type enum not found: " + typeEnumName));
            policy.setTypeEnumId(typeEnum.getId());
        }

        mapper.updateEntity(request, policy);
        LeavePolicy updatedPolicy = repository.save(policy);
        log.info("Leave policy updated: {}", id);
        return enrichResponse(mapper.toResponse(updatedPolicy));
    }

    @Override
    public void delete(UUID id) {
        log.info("Deleting leave policy: {}", id);

        LeavePolicy policy = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave policy not found"));

        policy.softDelete();
        repository.save(policy);
        log.info("Leave policy deleted: {}", id);
    }

    private LeavePolicyResponse enrichResponse(LeavePolicyResponse response) {
        if (response.getTypeEnumId() != null) {
            final UUID typeEnumId = response.getTypeEnumId();
            typeEnumRepository.findByIdAndIsDeletedFalse(typeEnumId)
                    .ifPresent(typeEnum -> response.setTypeEnumName(typeEnum.getEnumName()));
        }
        return response;
    }
}
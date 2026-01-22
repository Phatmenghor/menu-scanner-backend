package com.emenu.features.hr.service.impl;

import com.emenu.enums.hr.LeaveStatusEnum;
import com.emenu.exception.custom.BusinessValidationException;
import com.emenu.exception.custom.ResourceNotFoundException;
import com.emenu.features.auth.mapper.UserMapper;
import com.emenu.features.hr.dto.filter.LeaveFilterRequest;
import com.emenu.features.hr.dto.request.LeaveApprovalRequest;
import com.emenu.features.hr.dto.request.LeaveCreateRequest;
import com.emenu.features.hr.dto.response.LeaveResponse;
import com.emenu.features.hr.dto.update.LeaveUpdateRequest;
import com.emenu.features.hr.mapper.LeaveMapper;
import com.emenu.features.hr.models.Leave;
import com.emenu.features.hr.repository.LeaveRepository;
import com.emenu.features.hr.service.LeaveService;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.mapper.PaginationMapper;
import com.emenu.shared.pagination.PaginationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LeaveServiceImpl implements LeaveService {

    private final LeaveRepository repository;
    private final LeaveMapper mapper;
    private final PaginationMapper paginationMapper;
    private final UserMapper userMapper;

    /**
     * Creates a new leave request for an employee
     */
    @Override
    public LeaveResponse create(LeaveCreateRequest request, UUID userId, UUID businessId) {
        log.info("Creating leave request for user: {}", userId);

        double totalDays = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;

        Leave leave = mapper.toEntity(request);
        leave.setUserId(userId);
        leave.setBusinessId(businessId);
        leave.setTotalDays(totalDays);
        leave.setStatus(LeaveStatusEnum.PENDING);

        Leave savedLeave = repository.save(leave);
        log.info("Leave request created: {}", savedLeave.getId());
        return enrichWithUserInfo(mapper.toResponse(savedLeave), savedLeave);
    }

    /**
     * Retrieves a leave request by ID
     */
    @Override
    @Transactional(readOnly = true)
    public LeaveResponse getById(UUID id) {
        Leave leave = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave not found"));
        return enrichWithUserInfo(mapper.toResponse(leave), leave);
    }

    /**
     * Retrieves all leave requests with filtering and pagination support
     */
    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<LeaveResponse> getAll(LeaveFilterRequest filter) {
        Pageable pageable = PaginationUtils.createPageable(
                filter.getPageNo(),
                filter.getPageSize(),
                filter.getSortBy(),
                filter.getSortDirection()
        );

        // Convert empty lists to null to skip filtering
        List<LeaveStatusEnum> leaveStatusEnums = (filter.getStatuses() != null && !filter.getStatuses().isEmpty())
                ? filter.getStatuses() : null;

        Page<Leave> page = repository.findWithFilters(
                filter.getBusinessId(),
                filter.getUserId(),
                filter.getLeaveTypeEnum(),
                leaveStatusEnums,
                filter.getStartDate(),
                filter.getEndDate(),
                filter.getSearch(),
                pageable
        );

        return paginationMapper.toPaginationResponse(page,
                leaves -> leaves.stream()
                        .map(l -> enrichWithUserInfo(mapper.toResponse(l), l))
                        .toList());
    }

    /**
     * Updates a pending leave request
     */
    @Override
    public LeaveResponse update(UUID id, LeaveUpdateRequest request) {
        Leave leave = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave not found"));

        if (!leave.getStatus().isPending()) {
            throw new BusinessValidationException("Cannot update leave that is not pending");
        }

        mapper.updateEntity(request, leave);

        if (request.getStartDate() != null && request.getEndDate() != null) {
            double totalDays = ChronoUnit.DAYS.between(leave.getStartDate(), leave.getEndDate()) + 1;
            leave.setTotalDays(totalDays);
        }

        Leave updatedLeave = repository.save(leave);
        return enrichWithUserInfo(mapper.toResponse(updatedLeave), updatedLeave);
    }

    /**
     * Approves or rejects a leave request
     */
    @Override
    public LeaveResponse approve(UUID id, LeaveApprovalRequest request, UUID actionBy) {
        Leave leave = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave not found"));

        if (!leave.getStatus().isPending()) {
            throw new BusinessValidationException("Leave is not pending approval");
        }

        leave.setStatus(request.getStatus());
        leave.setActionBy(actionBy);
        leave.setActionAt(LocalDateTime.now());
        leave.setActionNote(request.getActionNote());

        Leave processedLeave = repository.save(leave);
        log.info("Leave {} {} by user: {}", id, request.getStatus(), actionBy);
        return enrichWithUserInfo(mapper.toResponse(processedLeave), processedLeave);
    }

    /**
     * Soft deletes a leave request
     */
    @Override
    public LeaveResponse delete(UUID id) {
        Leave leave = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave not found"));
        leave.softDelete();
        leave = repository.save(leave);
        return enrichWithUserInfo(mapper.toResponse(leave), leave);
    }

    private LeaveResponse enrichWithUserInfo(LeaveResponse response, Leave leave) {
        if (leave.getUser() != null) {
            response.setUserInfo(userMapper.toUserBasicInfo(leave.getUser()));
        }
        if (leave.getActionUser() != null) {
            response.setActionUserInfo(userMapper.toUserBasicInfo(leave.getActionUser()));
        }
        return response;
    }
}
package com.emenu.features.hr.service.impl;

import com.emenu.enums.hr.LeaveStatusEnum;
import com.emenu.exception.custom.BusinessValidationException;
import com.emenu.exception.custom.ResourceNotFoundException;
import com.emenu.features.hr.dto.filter.LeaveFilterRequest;
import com.emenu.features.hr.dto.request.LeaveApprovalRequest;
import com.emenu.features.hr.dto.request.LeaveCreateRequest;
import com.emenu.features.hr.dto.response.LeaveResponse;
import com.emenu.features.hr.dto.update.LeaveUpdateRequest;
import com.emenu.features.hr.mapper.LeaveMapper;
import com.emenu.features.hr.models.Leave;
import com.emenu.features.hr.models.LeaveBalance;
import com.emenu.features.hr.models.LeavePolicy;
import com.emenu.features.hr.repository.LeaveBalanceRepository;
import com.emenu.features.hr.repository.LeavePolicyRepository;
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

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LeaveServiceImpl implements LeaveService {

    private final LeaveRepository repository;
    private final LeavePolicyRepository policyRepository;
    private final LeaveBalanceRepository balanceRepository;
    private final LeaveMapper mapper;
    private final PaginationMapper paginationMapper;

    @Override
    public LeaveResponse create(LeaveCreateRequest request, UUID userId, UUID businessId) {
        log.info("Creating leave request for user: {}", userId);

        LeavePolicy policy = policyRepository.findByIdAndIsDeletedFalse(request.getPolicyId())
                .orElseThrow(() -> new ResourceNotFoundException("Leave policy not found"));

        double totalDays = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;

        if (policy.getMaxConsecutiveDays() != null && totalDays > policy.getMaxConsecutiveDays()) {
            throw new BusinessValidationException(
                    "Leave duration exceeds maximum consecutive days: " + policy.getMaxConsecutiveDays());
        }

        int year = request.getStartDate().getYear();
        LeaveBalance balance = balanceRepository.findByUserIdAndPolicyIdAndYearAndIsDeletedFalse(
                        userId, request.getPolicyId(), year)
                .orElseThrow(() -> new BusinessValidationException("No leave balance found for this year"));

        if (balance.getRemainingDays() < totalDays) {
            throw new BusinessValidationException(
                    "Insufficient leave balance. Remaining: " + balance.getRemainingDays() + " days");
        }

        Leave leave = mapper.toEntity(request);
        leave.setUserId(userId);
        leave.setBusinessId(businessId);
        leave.setTotalDays(totalDays);
        leave.setStatus(LeaveStatusEnum.PENDING);

        Leave savedLeave = repository.save(leave);
        log.info("Leave request created: {}", savedLeave.getId());
        return mapper.toResponse(savedLeave);
    }

    @Override
    @Transactional(readOnly = true)
    public LeaveResponse getById(UUID id) {
        Leave leave = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave not found"));
        return mapper.toResponse(leave);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<LeaveResponse> getAll(LeaveFilterRequest filter) {
        Pageable pageable = PaginationUtils.createPageable(
                filter.getPageNo(),
                filter.getPageSize(),
                filter.getSortBy(),
                filter.getSortDirection()
        );

        Page<Leave> page = repository.findWithFilters(
                filter.getBusinessId(),
                filter.getUserId(),
                filter.getPolicyId(),
                filter.getStartDate(),
                filter.getEndDate(),
                filter.getSearch(),
                pageable
        );

        return paginationMapper.toPaginationResponse(page,
                leaves -> leaves.stream().map(mapper::toResponse).toList());
    }

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
        return mapper.toResponse(updatedLeave);
    }

    @Override
    public LeaveResponse approve(UUID id, LeaveApprovalRequest request, UUID approvedBy) {
        Leave leave = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave not found"));

        if (!leave.getStatus().isPending()) {
            throw new BusinessValidationException("Leave is not pending approval");
        }

        LeaveStatusEnum newStatus = LeaveStatusEnum.valueOf(request.getStatus().toUpperCase());

        leave.setStatus(newStatus);
        leave.setApprovedBy(approvedBy);
        leave.setApprovedAt(ZonedDateTime.now());
        leave.setApproverNote(request.getApproverNote());

        if (newStatus.isApproved()) {
            int year = leave.getStartDate().getYear();
            LeaveBalance balance = balanceRepository.findByUserIdAndPolicyIdAndYearAndIsDeletedFalse(
                            leave.getUserId(), leave.getPolicyId(), year)
                    .orElseThrow(() -> new BusinessValidationException("Leave balance not found"));

            balance.setUsedDays(balance.getUsedDays() + leave.getTotalDays());
            balance.setRemainingDays(balance.getTotalAllowance() - balance.getUsedDays());
            balanceRepository.save(balance);
            
            log.info("Leave approved, balance updated for user: {}", leave.getUserId());
        }

        Leave approvedLeave = repository.save(leave);
        return mapper.toResponse(approvedLeave);
    }

    @Override
    public LeaveResponse delete(UUID id) {
        Leave leave = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave not found"));
        leave.softDelete();
        leave = repository.save(leave);
        return mapper.toResponse(leave);
    }
}
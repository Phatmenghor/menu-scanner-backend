package com.emenu.features.hr.service.impl;

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
import com.emenu.exception.custom.BusinessValidationException;
import com.emenu.exception.custom.ResourceNotFoundException;
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
    private final LeaveStatusEnumRepository statusEnumRepository;
    private final LeaveMapper mapper;
    private final PaginationMapper paginationMapper;

    @Override
    public LeaveResponse create(LeaveCreateRequest request, UUID userId, UUID businessId) {
        log.info("Creating leave request for user: {}", userId);

        // Validate policy
        LeavePolicy policy = policyRepository.findByIdAndIsDeletedFalse(request.getPolicyId())
                .orElseThrow(() -> new ResourceNotFoundException("Leave policy not found"));

        // Calculate total days
        double totalDays = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;

        // Validate against policy
        if (policy.getMaxConsecutiveDays() != null && totalDays > policy.getMaxConsecutiveDays()) {
            throw new BusinessValidationException(
                    "Leave duration exceeds maximum consecutive days: " + policy.getMaxConsecutiveDays());
        }

        // Validate advance notice
        if (policy.getMinAdvanceNoticeDays() != null) {
            long daysUntilStart = ChronoUnit.DAYS.between(LocalDate.now(), request.getStartDate());
            if (daysUntilStart < policy.getMinAdvanceNoticeDays()) {
                throw new BusinessValidationException(
                        "Leave requires minimum " + policy.getMinAdvanceNoticeDays() + " days advance notice");
            }
        }

        // Check balance
        int year = request.getStartDate().getYear();
        LeaveBalance balance = balanceRepository.findByUserIdAndPolicyIdAndYearAndIsDeletedFalse(
                        userId, request.getPolicyId(), year)
                .orElseThrow(() -> new BusinessValidationException("No leave balance found for this year"));

        if (balance.getRemainingDays() < totalDays) {
            throw new BusinessValidationException(
                    "Insufficient leave balance. Remaining: " + balance.getRemainingDays() + " days");
        }

        final Leave leave = mapper.toEntity(request);
        leave.setUserId(userId);
        leave.setBusinessId(businessId);
        leave.setTotalDays(totalDays);

        // Set initial status based on policy
        final UUID leaveBusinessId = businessId;
        if (policy.getRequiresApproval()) {
            statusEnumRepository.findByBusinessIdAndEnumNameAndIsDeletedFalse(leaveBusinessId, "PENDING")
                    .ifPresent(statusEnum -> leave.setStatusEnumId(statusEnum.getId()));
        } else {
            statusEnumRepository.findByBusinessIdAndEnumNameAndIsDeletedFalse(leaveBusinessId, "APPROVED")
                    .ifPresent(statusEnum -> leave.setStatusEnumId(statusEnum.getId()));
        }

        Leave savedLeave = repository.save(leave);
        return enrichResponse(mapper.toResponse(savedLeave));
    }

    @Override
    @Transactional(readOnly = true)
    public LeaveResponse getById(UUID id) {
        Leave leave = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave not found"));
        return enrichResponse(mapper.toResponse(leave));
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
                filter.getStatusEnumId(),
                filter.getStartDate(),
                filter.getEndDate(),
                filter.getSearch(),
                pageable
        );

        return paginationMapper.toPaginationResponse(page,
                leaves -> leaves.stream()
                        .map(mapper::toResponse)
                        .map(this::enrichResponse)
                        .toList());
    }

    @Override
    public LeaveResponse update(UUID id, LeaveUpdateRequest request) {
        final Leave leave = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave not found"));

        // Update status enum if provided
        if (request.getStatusEnumName() != null) {
            final UUID leaveBusinessId = leave.getBusinessId();
            final String statusEnumName = request.getStatusEnumName();

            statusEnumRepository.findByBusinessIdAndEnumNameAndIsDeletedFalse(
                            leaveBusinessId, statusEnumName)
                    .ifPresent(statusEnum -> leave.setStatusEnumId(statusEnum.getId()));
        }

        mapper.updateEntity(request, leave);

        // Recalculate total days if dates changed
        if (request.getStartDate() != null && request.getEndDate() != null) {
            double totalDays = ChronoUnit.DAYS.between(leave.getStartDate(), leave.getEndDate()) + 1;
            leave.setTotalDays(totalDays);
        }

        Leave updatedLeave = repository.save(leave);
        return enrichResponse(mapper.toResponse(updatedLeave));
    }

    @Override
    public LeaveResponse approve(UUID id, LeaveApprovalRequest request, UUID approvedBy) {
        final Leave leave = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave not found"));

        // Set status
        final UUID leaveBusinessId = leave.getBusinessId();
        final String statusEnumName = request.getStatusEnumName();

        statusEnumRepository.findByBusinessIdAndEnumNameAndIsDeletedFalse(
                        leaveBusinessId, statusEnumName)
                .ifPresent(statusEnum -> leave.setStatusEnumId(statusEnum.getId()));

        leave.setApprovedBy(approvedBy);
        leave.setApprovedAt(ZonedDateTime.now());
        leave.setApproverNote(request.getApproverNote());

        // Update balance if approved
        if ("APPROVED".equalsIgnoreCase(request.getStatusEnumName())) {
            int year = leave.getStartDate().getYear();
            LeaveBalance balance = balanceRepository.findByUserIdAndPolicyIdAndYearAndIsDeletedFalse(
                            leave.getUserId(), leave.getPolicyId(), year)
                    .orElseThrow(() -> new BusinessValidationException("Leave balance not found"));

            balance.setUsedDays(balance.getUsedDays() + leave.getTotalDays());
            balance.setRemainingDays(balance.getTotalAllowance() - balance.getUsedDays());
            balanceRepository.save(balance);
        }

        Leave approvedLeave = repository.save(leave);
        return enrichResponse(mapper.toResponse(approvedLeave));
    }

    @Override
    public LeaveResponse delete(UUID id) {
        Leave leave = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave not found"));
        leave.softDelete();
        leave = repository.save(leave);
        return enrichResponse(mapper.toResponse(leave));
    }

    private LeaveResponse enrichResponse(LeaveResponse response) {
        if (response.getStatusEnumId() != null) {
            final UUID statusEnumId = response.getStatusEnumId();
            statusEnumRepository.findByIdAndIsDeletedFalse(statusEnumId)
                    .ifPresent(statusEnum -> response.setStatusEnumName(statusEnum.getEnumName()));
        }
        return response;
    }
}
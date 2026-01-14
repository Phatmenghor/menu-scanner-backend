package com.emenu.features.leave.service.impl;

import com.emenu.enums.leave.LeaveStatus;
import com.emenu.exception.BadRequestException;
import com.emenu.exception.ResourceNotFoundException;
import com.emenu.features.auth.models.User;
import com.emenu.features.auth.repository.UserRepository;
import com.emenu.features.leave.dto.filter.LeaveFilterRequest;
import com.emenu.features.leave.dto.request.LeaveApprovalRequest;
import com.emenu.features.leave.dto.request.LeaveCreateRequest;
import com.emenu.features.leave.dto.response.LeaveBalanceResponse;
import com.emenu.features.leave.dto.response.LeaveResponse;
import com.emenu.features.leave.mapper.LeaveBalanceMapper;
import com.emenu.features.leave.mapper.LeaveMapper;
import com.emenu.features.leave.models.Leave;
import com.emenu.features.leave.models.LeaveBalance;
import com.emenu.features.leave.models.LeavePolicy;
import com.emenu.features.leave.repository.LeaveBalanceRepository;
import com.emenu.features.leave.repository.LeavePolicyRepository;
import com.emenu.features.leave.repository.LeaveRepository;
import com.emenu.features.leave.service.LeaveService;
import com.emenu.features.leave.specification.LeaveSpecification;
import com.emenu.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaveServiceImpl implements LeaveService {

    private final LeaveRepository leaveRepository;
    private final LeavePolicyRepository policyRepository;
    private final LeaveBalanceRepository balanceRepository;
    private final UserRepository userRepository;
    private final LeaveMapper leaveMapper;
    private final LeaveBalanceMapper balanceMapper;

    @Override
    @Transactional
    public LeaveResponse createLeaveRequest(LeaveCreateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        LeavePolicy policy = policyRepository.findById(request.getLeavePolicyId())
                .orElseThrow(() -> new ResourceNotFoundException("Leave policy not found"));

        // Validate dates
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("End date cannot be before start date");
        }

        // Calculate total days
        long daysBetween = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;
        double totalDays = daysBetween * request.getSession().getDays();

        // Validate consecutive days
        if (totalDays > policy.getMaxConsecutiveDays()) {
            throw new BadRequestException(
                    String.format("Cannot request more than %.1f consecutive days", policy.getMaxConsecutiveDays())
            );
        }

        // Validate advance notice
        long daysUntilStart = ChronoUnit.DAYS.between(LocalDate.now(), request.getStartDate());
        if (daysUntilStart < policy.getMinAdvanceNoticeDays()) {
            throw new BadRequestException(
                    String.format("Leave must be requested at least %d days in advance", policy.getMinAdvanceNoticeDays())
            );
        }

        // Check for overlapping leaves
        List<Leave> overlappingLeaves = leaveRepository.findOverlappingLeaves(
                userId, request.getStartDate(), request.getEndDate()
        );
        if (!overlappingLeaves.isEmpty()) {
            throw new BadRequestException("Leave request overlaps with existing leave");
        }

        // Check leave balance
        int currentYear = LocalDate.now().getYear();
        LeaveBalance balance = balanceRepository.findByUserIdAndLeavePolicyIdAndYear(userId, policy.getId(), currentYear)
                .orElseGet(() -> createLeaveBalance(user, policy, currentYear));

        if (balance.getRemainingDays() < totalDays) {
            throw new BadRequestException(
                    String.format("Insufficient leave balance. Available: %.1f days, Requested: %.1f days",
                            balance.getRemainingDays(), totalDays)
            );
        }

        // Create leave request
        Leave leave = leaveMapper.toEntity(request, user, policy);
        Leave savedLeave = leaveRepository.save(leave);

        // If no approval required, auto-approve and deduct balance
        if (!policy.getRequiresApproval()) {
            savedLeave.setStatus(LeaveStatus.APPROVED);
            savedLeave.setApprovedBy(user);
            savedLeave.setApprovedAt(ZonedDateTime.now());
            savedLeave = leaveRepository.save(savedLeave);

            // Deduct from balance
            balance.setUsedDays(balance.getUsedDays() + totalDays);
            balance.setRemainingDays(balance.getRemainingDays() - totalDays);
            balanceRepository.save(balance);
        }

        return leaveMapper.toResponse(savedLeave);
    }

    @Override
    @Transactional
    public LeaveResponse approveOrRejectLeave(Long leaveId, LeaveApprovalRequest request) {
        Long approverId = SecurityUtils.getCurrentUserId();

        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new ResourceNotFoundException("Approver not found"));

        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found"));

        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new BadRequestException("Leave request has already been processed");
        }

        if (request.getStatus() != LeaveStatus.APPROVED && request.getStatus() != LeaveStatus.REJECTED) {
            throw new BadRequestException("Invalid status. Must be APPROVED or REJECTED");
        }

        leave.setStatus(request.getStatus());
        leave.setApprovedBy(approver);
        leave.setApprovedAt(ZonedDateTime.now());
        leave.setApproverNote(request.getApproverNote());

        // If approved, deduct from balance
        if (request.getStatus() == LeaveStatus.APPROVED) {
            int year = leave.getStartDate().getYear();
            LeaveBalance balance = balanceRepository.findByUserIdAndLeavePolicyIdAndYear(
                    leave.getUser().getId(), leave.getLeavePolicy().getId(), year
            ).orElseThrow(() -> new ResourceNotFoundException("Leave balance not found"));

            if (balance.getRemainingDays() < leave.getTotalDays()) {
                throw new BadRequestException("Insufficient leave balance");
            }

            balance.setUsedDays(balance.getUsedDays() + leave.getTotalDays());
            balance.setRemainingDays(balance.getRemainingDays() - leave.getTotalDays());
            balanceRepository.save(balance);
        }

        Leave updatedLeave = leaveRepository.save(leave);
        return leaveMapper.toResponse(updatedLeave);
    }

    @Override
    @Transactional
    public LeaveResponse cancelLeave(Long leaveId) {
        Long userId = SecurityUtils.getCurrentUserId();

        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found"));

        if (!leave.getUser().getId().equals(userId)) {
            throw new BadRequestException("You can only cancel your own leave requests");
        }

        if (leave.getStatus() == LeaveStatus.CANCELLED) {
            throw new BadRequestException("Leave is already cancelled");
        }

        // If leave was approved, restore the balance
        if (leave.getStatus() == LeaveStatus.APPROVED) {
            int year = leave.getStartDate().getYear();
            LeaveBalance balance = balanceRepository.findByUserIdAndLeavePolicyIdAndYear(
                    userId, leave.getLeavePolicy().getId(), year
            ).orElseThrow(() -> new ResourceNotFoundException("Leave balance not found"));

            balance.setUsedDays(balance.getUsedDays() - leave.getTotalDays());
            balance.setRemainingDays(balance.getRemainingDays() + leave.getTotalDays());
            balanceRepository.save(balance);
        }

        leave.setStatus(LeaveStatus.CANCELLED);
        Leave updatedLeave = leaveRepository.save(leave);

        return leaveMapper.toResponse(updatedLeave);
    }

    @Override
    @Transactional(readOnly = true)
    public LeaveResponse getLeaveById(Long id) {
        Leave leave = leaveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found"));

        return leaveMapper.toResponse(leave);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveResponse> getLeavesByUserId(Long userId) {
        return leaveRepository.findByUserId(userId).stream()
                .map(leaveMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LeaveResponse> getAllLeaves(LeaveFilterRequest filterRequest, Pageable pageable) {
        Specification<Leave> spec = LeaveSpecification.withFilters(filterRequest);
        return leaveRepository.findAll(spec, pageable)
                .map(leaveMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveBalanceResponse> getUserLeaveBalances(Long userId) {
        return balanceRepository.findByUserId(userId).stream()
                .map(balanceMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public LeaveBalanceResponse initializeLeaveBalance(Long userId, Long leavePolicyId, Integer year) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        LeavePolicy policy = policyRepository.findById(leavePolicyId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave policy not found"));

        // Check if balance already exists
        balanceRepository.findByUserIdAndLeavePolicyIdAndYear(userId, leavePolicyId, year)
                .ifPresent(b -> {
                    throw new BadRequestException("Leave balance already exists for this year");
                });

        LeaveBalance balance = createLeaveBalance(user, policy, year);
        return balanceMapper.toResponse(balance);
    }

    private LeaveBalance createLeaveBalance(User user, LeavePolicy policy, Integer year) {
        LeaveBalance balance = LeaveBalance.builder()
                .user(user)
                .leavePolicy(policy)
                .year(year)
                .totalAllowance(policy.getAnnualAllowance())
                .usedDays(0.0)
                .remainingDays(policy.getAnnualAllowance())
                .build();

        return balanceRepository.save(balance);
    }
}


package com.emenu.features.hr.service.impl;

import com.emenu.exception.custom.ResourceNotFoundException;
import com.emenu.features.hr.dto.response.LeaveBalanceResponse;
import com.emenu.features.hr.mapper.LeaveBalanceMapper;
import com.emenu.features.hr.models.LeaveBalance;
import com.emenu.features.hr.repository.LeaveBalanceRepository;
import com.emenu.features.hr.service.LeaveBalanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LeaveBalanceServiceImpl implements LeaveBalanceService {

    private final LeaveBalanceRepository repository;
    private final LeaveBalanceMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public LeaveBalanceResponse getById(UUID id) {
        LeaveBalance balance = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave balance not found"));
        return mapper.toResponse(balance);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveBalanceResponse> getByUserId(UUID userId) {
        List<LeaveBalance> balances = repository.findByUserIdAndIsDeletedFalse(userId);
        return mapper.toResponseList(balances);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveBalanceResponse> getByUserIdAndYear(UUID userId, Integer year) {
        List<LeaveBalance> balances = repository.findByUserIdAndYearAndIsDeletedFalse(userId, year);
        return mapper.toResponseList(balances);
    }

    @Override
    @Transactional(readOnly = true)
    public LeaveBalanceResponse getByUserIdPolicyIdAndYear(UUID userId, UUID policyId, Integer year) {
        LeaveBalance balance = repository.findByUserIdAndPolicyIdAndYearAndIsDeletedFalse(userId, policyId, year)
                .orElseThrow(() -> new ResourceNotFoundException("Leave balance not found"));
        return mapper.toResponse(balance);
    }

    @Override
    public LeaveBalanceResponse createOrUpdateBalance(UUID userId, UUID policyId, Integer year, Double allowance) {
        log.info("Creating/updating leave balance for user: {}, policy: {}, year: {}", userId, policyId, year);

        LeaveBalance balance = repository.findByUserIdAndPolicyIdAndYearAndIsDeletedFalse(userId, policyId, year)
                .orElseGet(() -> {
                    LeaveBalance newBalance = LeaveBalance.builder()
                            .userId(userId)
                            .policyId(policyId)
                            .year(year)
                            .totalAllowance(allowance)
                            .usedDays(0.0)
                            .remainingDays(allowance)
                            .carriedForwardDays(0.0)
                            .build();
                    return repository.save(newBalance);
                });

        // Update allowance and remaining days if balance already exists
        if (balance.getTotalAllowance() != null && !balance.getTotalAllowance().equals(allowance)) {
            double difference = allowance - balance.getTotalAllowance();
            balance.setTotalAllowance(allowance);
            balance.setRemainingDays(balance.getRemainingDays() + difference);
            balance = repository.save(balance);
        }

        return mapper.toResponse(balance);
    }

    @Override
    public void resetYearlyBalance(UUID userId, UUID policyId, Integer year) {
        log.info("Resetting leave balance for user: {}, policy: {}, year: {}", userId, policyId, year);

        LeaveBalance balance = repository.findByUserIdAndPolicyIdAndYearAndIsDeletedFalse(userId, policyId, year)
                .orElseThrow(() -> new ResourceNotFoundException("Leave balance not found"));

        // Reset used days and recalculate remaining
        balance.setUsedDays(0.0);
        balance.setRemainingDays(balance.getTotalAllowance());
        balance.setCarriedForwardDays(0.0);

        repository.save(balance);
        log.info("Leave balance reset for user: {}", userId);
    }
}
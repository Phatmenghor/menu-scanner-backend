package com.emenu.features.hr.service;

import com.emenu.features.hr.dto.response.LeaveBalanceResponse;

import java.util.List;
import java.util.UUID;

public interface LeaveBalanceService {
    LeaveBalanceResponse getById(UUID id);
    List<LeaveBalanceResponse> getByUserId(UUID userId);
    List<LeaveBalanceResponse> getByUserIdAndYear(UUID userId, Integer year);
    LeaveBalanceResponse getByUserIdPolicyIdAndYear(UUID userId, UUID policyId, Integer year);
    LeaveBalanceResponse createOrUpdateBalance(UUID userId, UUID policyId, Integer year, Double allowance);
    LeaveBalanceResponse resetYearlyBalance(UUID userId, UUID policyId, Integer year);
}
package com.emenu.features.hr.controller;

import com.emenu.features.hr.dto.response.LeaveBalanceResponse;
import com.emenu.features.hr.service.LeaveBalanceService;
import com.emenu.security.SecurityUtils;
import com.emenu.shared.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hr/leave-balance")
@RequiredArgsConstructor
@Slf4j
public class LeaveBalanceController {

    private final LeaveBalanceService service;
    private final SecurityUtils securityUtils;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LeaveBalanceResponse>> getById(@PathVariable UUID id) {
        log.info("Get leave balance: {}", id);
        LeaveBalanceResponse response = service.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Leave balance retrieved", response));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<LeaveBalanceResponse>>> getByUserId(
            @PathVariable UUID userId) {
        log.info("Get all leave balances for user: {}", userId);
        List<LeaveBalanceResponse> responses = service.getByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success("Leave balances retrieved", responses));
    }

    @GetMapping("/user/{userId}/year/{year}")
    public ResponseEntity<ApiResponse<List<LeaveBalanceResponse>>> getByUserIdAndYear(
            @PathVariable UUID userId,
            @PathVariable Integer year) {
        log.info("Get leave balances for user: {} in year: {}", userId, year);
        List<LeaveBalanceResponse> responses = service.getByUserIdAndYear(userId, year);
        return ResponseEntity.ok(ApiResponse.success("Leave balances retrieved", responses));
    }

    @GetMapping("/user/{userId}/policy/{policyId}/year/{year}")
    public ResponseEntity<ApiResponse<LeaveBalanceResponse>> getByUserIdPolicyIdAndYear(
            @PathVariable UUID userId,
            @PathVariable UUID policyId,
            @PathVariable Integer year) {
        log.info("Get leave balance for user: {}, policy: {}, year: {}", userId, policyId, year);
        LeaveBalanceResponse response = service.getByUserIdPolicyIdAndYear(userId, policyId, year);
        return ResponseEntity.ok(ApiResponse.success("Leave balance retrieved", response));
    }

    @PostMapping("/user/{userId}/policy/{policyId}/year/{year}")
    public ResponseEntity<ApiResponse<LeaveBalanceResponse>> createOrUpdateBalance(
            @PathVariable UUID userId,
            @PathVariable UUID policyId,
            @PathVariable Integer year,
            @RequestParam Double allowance) {
        log.info("Creating/updating leave balance for user: {}, policy: {}, year: {}, allowance: {}",
                userId, policyId, year, allowance);
        LeaveBalanceResponse response = service.createOrUpdateBalance(userId, policyId, year, allowance);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Leave balance created/updated", response));
    }

    @PostMapping("/user/{userId}/policy/{policyId}/year/{year}/reset")
    public ResponseEntity<ApiResponse<LeaveBalanceResponse>> resetYearlyBalance(
            @PathVariable UUID userId,
            @PathVariable UUID policyId,
            @PathVariable Integer year) {
        log.info("Resetting leave balance for user: {}, policy: {}, year: {}", userId, policyId, year);
        LeaveBalanceResponse response = service.resetYearlyBalance(userId, policyId, year);
        return ResponseEntity.ok(ApiResponse.success("Leave balance reset", response));
    }

    @GetMapping("/my-balances")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'BUSINESS_STAFF', 'BUSINESS_MANAGER', 'BUSINESS_OWNER')")
    public ResponseEntity<ApiResponse<List<LeaveBalanceResponse>>> getMyBalances() {
        log.info("Get current user's leave balances");
        UUID userId = securityUtils.getCurrentUserId();
        List<LeaveBalanceResponse> responses = service.getByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success("My leave balances retrieved", responses));
    }

    @GetMapping("/my-balances/year/{year}")
    public ResponseEntity<ApiResponse<List<LeaveBalanceResponse>>> getMyBalancesForYear(
            @PathVariable Integer year) {
        log.info("Get current user's leave balances for year: {}", year);
        UUID userId = securityUtils.getCurrentUserId();
        List<LeaveBalanceResponse> responses = service.getByUserIdAndYear(userId, year);
        return ResponseEntity.ok(ApiResponse.success("My leave balances retrieved", responses));
    }
}
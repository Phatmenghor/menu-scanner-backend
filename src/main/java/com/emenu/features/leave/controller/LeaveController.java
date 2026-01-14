package com.emenu.features.leave.controller;

import com.emenu.features.leave.dto.filter.LeaveFilterRequest;
import com.emenu.features.leave.dto.request.LeaveApprovalRequest;
import com.emenu.features.leave.dto.request.LeaveCreateRequest;
import com.emenu.features.leave.dto.response.LeaveBalanceResponse;
import com.emenu.features.leave.dto.response.LeaveResponse;
import com.emenu.features.leave.service.LeaveService;
import com.emenu.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/leaves")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;

    @PostMapping
    public ResponseEntity<LeaveResponse> createLeaveRequest(@Valid @RequestBody LeaveCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(leaveService.createLeaveRequest(request));
    }

    @PostMapping("/{leaveId}/approval")
    public ResponseEntity<LeaveResponse> approveOrRejectLeave(
            @PathVariable Long leaveId,
            @Valid @RequestBody LeaveApprovalRequest request) {
        return ResponseEntity.ok(leaveService.approveOrRejectLeave(leaveId, request));
    }

    @PostMapping("/{leaveId}/cancel")
    public ResponseEntity<LeaveResponse> cancelLeave(@PathVariable Long leaveId) {
        return ResponseEntity.ok(leaveService.cancelLeave(leaveId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeaveResponse> getLeaveById(@PathVariable Long id) {
        return ResponseEntity.ok(leaveService.getLeaveById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<LeaveResponse>> getLeavesByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(leaveService.getLeavesByUserId(userId));
    }

    @GetMapping("/my-leaves")
    public ResponseEntity<List<LeaveResponse>> getMyLeaves() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(leaveService.getLeavesByUserId(userId));
    }

    @GetMapping
    public ResponseEntity<Page<LeaveResponse>> getAllLeaves(
            LeaveFilterRequest filterRequest,
            Pageable pageable) {
        return ResponseEntity.ok(leaveService.getAllLeaves(filterRequest, pageable));
    }

    @GetMapping("/balances/user/{userId}")
    public ResponseEntity<List<LeaveBalanceResponse>> getUserLeaveBalances(@PathVariable Long userId) {
        return ResponseEntity.ok(leaveService.getUserLeaveBalances(userId));
    }

    @GetMapping("/balances/my-balances")
    public ResponseEntity<List<LeaveBalanceResponse>> getMyLeaveBalances() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(leaveService.getUserLeaveBalances(userId));
    }

    @PostMapping("/balances/initialize")
    public ResponseEntity<LeaveBalanceResponse> initializeLeaveBalance(
            @RequestParam Long userId,
            @RequestParam Long leavePolicyId,
            @RequestParam Integer year) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(leaveService.initializeLeaveBalance(userId, leavePolicyId, year));
    }
}

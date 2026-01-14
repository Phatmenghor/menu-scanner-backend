package com.emenu.features.leave.service;

import com.emenu.features.leave.dto.filter.LeaveFilterRequest;
import com.emenu.features.leave.dto.request.LeaveApprovalRequest;
import com.emenu.features.leave.dto.request.LeaveCreateRequest;
import com.emenu.features.leave.dto.response.LeaveBalanceResponse;
import com.emenu.features.leave.dto.response.LeaveResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface LeaveService {

    LeaveResponse createLeaveRequest(LeaveCreateRequest request);

    LeaveResponse approveOrRejectLeave(Long leaveId, LeaveApprovalRequest request);

    LeaveResponse cancelLeave(Long leaveId);

    LeaveResponse getLeaveById(Long id);

    List<LeaveResponse> getLeavesByUserId(Long userId);

    Page<LeaveResponse> getAllLeaves(LeaveFilterRequest filterRequest, Pageable pageable);

    List<LeaveBalanceResponse> getUserLeaveBalances(Long userId);

    LeaveBalanceResponse initializeLeaveBalance(Long userId, Long leavePolicyId, Integer year);
}

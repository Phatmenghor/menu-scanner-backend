package com.emenu.features.leave.dto.filter;

import com.emenu.enums.leave.LeaveStatus;
import com.emenu.enums.leave.LeaveType;
import com.emenu.shared.dto.BaseAllFilterRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LeaveFilterRequest extends BaseAllFilterRequest {

    private Long userId;
    private Long businessId;
    private Long leavePolicyId;
    private LeaveType leaveType;
    private LeaveStatus status;
    private LocalDate fromDate;
    private LocalDate toDate;
}

package com.emenu.features.leave.dto.request;

import com.emenu.enums.leave.LeaveStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveApprovalRequest {

    @NotNull(message = "Status is required (APPROVED or REJECTED)")
    private LeaveStatus status;

    private String approverNote;
}

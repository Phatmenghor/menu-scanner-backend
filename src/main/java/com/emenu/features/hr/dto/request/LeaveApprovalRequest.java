package com.emenu.features.hr.dto.request;

import com.emenu.enums.hr.LeaveStatusEnum;
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
    @NotNull(message = "Status is required")
    private LeaveStatusEnum status;

    private String actionNote;
}
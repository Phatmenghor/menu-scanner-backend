package com.emenu.features.hr.dto.request;

import com.emenu.enums.hr.LeaveStatusEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveApprovalRequest {
    @NotBlank(message = "Status required")
    private LeaveStatusEnum status;

    private String actionNote;
}
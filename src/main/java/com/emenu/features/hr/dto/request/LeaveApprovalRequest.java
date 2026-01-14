package com.emenu.features.hr.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveApprovalRequest {
    @NotBlank(message = "Status enum name required")
    private String statusEnumName;
    
    private String approverNote;
}
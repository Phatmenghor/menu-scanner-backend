package com.emenu.features.leave.dto.request;

import com.emenu.enums.leave.LeaveSession;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveCreateRequest {

    @NotNull(message = "Leave policy ID is required")
    private Long leavePolicyId;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @NotNull(message = "Session is required")
    private LeaveSession session;

    private String reason;
}

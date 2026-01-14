package com.emenu.features.hr.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveCreateRequest {
    @NotNull(message = "Policy ID required")
    private UUID policyId;
    
    @NotNull(message = "Start date required")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    
    @NotNull(message = "End date required")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    
    @NotBlank(message = "Reason required")
    private String reason;
}
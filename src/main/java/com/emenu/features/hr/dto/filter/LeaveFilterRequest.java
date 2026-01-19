package com.emenu.features.hr.dto.filter;

import com.emenu.shared.dto.BaseFilterRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
public class LeaveFilterRequest extends BaseFilterRequest {
    private UUID businessId;
    private UUID userId;
    private String leaveTypeEnum;
    private LocalDate startDate;
    private LocalDate endDate;
}
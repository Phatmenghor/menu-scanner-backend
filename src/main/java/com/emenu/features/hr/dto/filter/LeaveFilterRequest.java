package com.emenu.features.hr.dto.filter;

import com.emenu.enums.hr.LeaveStatusEnum;
import com.emenu.shared.dto.BaseFilterRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
public class LeaveFilterRequest extends BaseFilterRequest {
    private UUID businessId;
    private UUID userId;
    private String leaveTypeEnum;
    private List<LeaveStatusEnum> statuses;
    private LocalDate startDate;
    private LocalDate endDate;
}
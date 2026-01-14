package com.emenu.features.hr.dto.filter;

import com.emenu.shared.dto.BaseFilterRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
public class WorkScheduleFilterRequest extends BaseFilterRequest {
    private UUID businessId;
    private UUID userId;
    private UUID policyId;
}
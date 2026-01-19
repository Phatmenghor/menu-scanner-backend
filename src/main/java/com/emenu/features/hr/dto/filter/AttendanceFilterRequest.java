package com.emenu.features.hr.dto.filter;

import com.emenu.shared.dto.BaseFilterRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
public class AttendanceFilterRequest extends BaseFilterRequest {
    private UUID businessId;
    private UUID userId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String statusEnum;
}
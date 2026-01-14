package com.emenu.features.attendance.dto.filter;

import com.emenu.enums.attendance.AttendanceStatus;
import com.emenu.shared.dto.BaseAllFilterRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AttendanceFilterRequest extends BaseAllFilterRequest {

    private Long userId;
    private Long businessId;
    private Long workScheduleId;
    private AttendanceStatus status;
    private LocalDate fromDate;
    private LocalDate toDate;
}

package com.emenu.features.hr.dto.response;

import com.emenu.features.auth.dto.response.UserBasicInfo;
import com.emenu.shared.dto.BaseAuditResponse;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkScheduleResponse extends BaseAuditResponse {
    private UserBasicInfo userInfo;
    private UUID businessId;
    private String name;
    private String scheduleTypeEnumName;
    private Set<DayOfWeek> workDays;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalTime breakStartTime;
    private LocalTime breakEndTime;
}
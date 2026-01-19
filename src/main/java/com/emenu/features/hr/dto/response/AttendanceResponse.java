package com.emenu.features.hr.dto.response;

import com.emenu.enums.hr.AttendanceStatusEnum;
import com.emenu.features.auth.dto.response.UserBasicInfo;
import com.emenu.shared.dto.BaseAuditResponse;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceResponse extends BaseAuditResponse {
    private UUID id;
    private UserBasicInfo userInfo;
    private UUID businessId;
    private UUID workScheduleId;
    private LocalDate attendanceDate;
    private List<AttendanceCheckInResponse> checkIns;
    private AttendanceStatusEnum status;
    private String remarks;
}
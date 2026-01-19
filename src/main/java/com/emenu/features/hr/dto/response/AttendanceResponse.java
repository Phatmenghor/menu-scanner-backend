package com.emenu.features.hr.dto.response;

import com.emenu.enums.hr.AttendanceStatusEnum;
import com.emenu.features.auth.dto.response.UserBasicInfo;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceResponse {
    private UUID id;
    private UserBasicInfo userInfo;
    private UUID businessId;
    private UUID workScheduleId;

    private LocalDate attendanceDate;

    private List<AttendanceCheckInResponse> checkIns;

    private AttendanceStatusEnum status;
    private String remarks;
}
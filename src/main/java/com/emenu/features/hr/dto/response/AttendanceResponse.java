package com.emenu.features.hr.dto.response;

import com.emenu.enums.hr.AttendanceStatusEnum;
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
    private UUID userId;
    private UUID businessId;
    private UUID workScheduleId;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate attendanceDate;
    
    private List<AttendanceCheckInResponse> checkIns;
    
    private Integer totalWorkMinutes;
    private Integer lateMinutes;
    
    private AttendanceStatusEnum status;
    private String remarks;
}
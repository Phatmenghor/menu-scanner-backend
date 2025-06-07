package com.menghor.ksit.feature.attendance.dto.request;

import com.menghor.ksit.enumations.AttendanceFinalizationStatus;
import com.menghor.ksit.enumations.AttendanceStatus;
import com.menghor.ksit.enumations.SemesterEnum;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AttendanceHistoryFilterDto {
    private String search;
    private Long scheduleId;
    private Long classId;
    private Long teacherId;
    private AttendanceFinalizationStatus finalizationStatus;
    private AttendanceStatus status;
    private SemesterEnum semester;
    private Integer academyYear;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer pageNo = 1;
    private Integer pageSize = 10;
}
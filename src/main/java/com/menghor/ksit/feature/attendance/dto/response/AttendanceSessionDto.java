package com.menghor.ksit.feature.attendance.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.menghor.ksit.enumations.AttendanceFinalizationStatus;
import com.menghor.ksit.enumations.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceSessionDto {
    private Long id;
    private LocalDateTime sessionDate;
    private AttendanceFinalizationStatus finalizationStatus;
    private Status status;
    private Long scheduleId;
    private String roomName;
    private String classCode;
    private Long teacherId;
    private String teacherName;
    private Long totalStudents;
    private Long totalPresent;
    private Long totalAbsent;
    private String createdAt;
    private List<AttendanceDto> attendances;
}
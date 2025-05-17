package com.menghor.ksit.feature.attendance.dto.update;

import com.menghor.ksit.enumations.AttendanceStatus;
import com.menghor.ksit.enumations.AttendanceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceUpdateRequest {
    private Long id;
    private AttendanceStatus status;
    private AttendanceType attendanceType;
    private String comment;
}
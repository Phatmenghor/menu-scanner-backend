package com.menghor.ksit.feature.attendance.dto.request;

import com.menghor.ksit.enumations.AttendanceStatus;
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
    private String comment;
}
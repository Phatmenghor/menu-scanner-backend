package com.menghor.ksit.feature.attendance.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QrAttendanceRequest {
    private String qrCode;
    private Long studentId;
}
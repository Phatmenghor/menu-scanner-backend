package com.emenu.features.attendance.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceCheckOutRequest {

    private Double latitude;
    private Double longitude;
    private String address;
    private String note;
}

package com.emenu.features.attendance.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceCheckInRequest {

    @NotNull(message = "Work schedule ID is required")
    private Long workScheduleId;

    private Double latitude;
    private Double longitude;
    private String address;
    private String note;
}

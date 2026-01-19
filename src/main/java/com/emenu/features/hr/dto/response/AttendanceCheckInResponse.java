package com.emenu.features.hr.dto.response;

import com.emenu.enums.hr.CheckInType;
import com.emenu.shared.dto.BaseAuditResponse;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceCheckInResponse extends BaseAuditResponse {
    private UUID id;
    private CheckInType checkInType;
    private LocalDateTime checkInTime;
    private Double latitude;
    private Double longitude;
    private String remarks;
}

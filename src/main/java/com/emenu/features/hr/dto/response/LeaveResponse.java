package com.emenu.features.hr.dto.response;

import com.emenu.enums.hr.LeaveStatusEnum;
import com.emenu.features.auth.dto.response.UserBasicInfo;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveResponse {
    private UUID id;
    private UUID userId;
    private UserBasicInfo userInfo;
    private UUID businessId;
    private String leaveTypeEnum;

    private LocalDate startDate;
    private LocalDate endDate;

    private Double totalDays;
    private String reason;

    private LeaveStatusEnum status;

    private UUID actionBy;
    private UserBasicInfo actionUserInfo;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime actionAt;

    private String actionNote;
}
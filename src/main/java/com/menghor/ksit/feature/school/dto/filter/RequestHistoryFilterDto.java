package com.menghor.ksit.feature.school.dto.filter;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.menghor.ksit.enumations.RequestStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RequestHistoryFilterDto {
    private String search;
    private Long userId;
    // Filter by the user who PERFORMED the action (staff/admin who processed the request)
    private Long actionUserId;
    // Filter by specific request ID
    private Long requestId;
    // Filter by the user who MADE the original request (we get this through request.user.id)
    private RequestStatus status;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    private int pageNo = 1;
    private int pageSize = 10;
}
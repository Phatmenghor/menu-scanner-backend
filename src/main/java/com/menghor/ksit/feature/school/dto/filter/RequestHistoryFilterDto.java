package com.menghor.ksit.feature.school.dto.filter;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.menghor.ksit.enumations.RequestStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RequestHistoryFilterDto {
    
    private Long requestId;
    
    private String search;

    private RequestStatus status;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private int pageNo = 1;
    
    private int pageSize = 10;
}
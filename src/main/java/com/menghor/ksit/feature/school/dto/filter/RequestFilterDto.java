package com.menghor.ksit.feature.school.dto.filter;

import com.menghor.ksit.enumations.RequestStatus;
import lombok.Data;

@Data
public class RequestFilterDto {
    private String search;
    private RequestStatus status;
    private Long userId;
    private Integer pageNo = 1;
    private Integer pageSize = 10;
}
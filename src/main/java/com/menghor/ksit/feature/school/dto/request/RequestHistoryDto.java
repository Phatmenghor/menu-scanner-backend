package com.menghor.ksit.feature.school.dto.request;

import com.menghor.ksit.enumations.RequestStatus;
import com.menghor.ksit.feature.school.dto.response.UserBasicInfoDto;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RequestHistoryDto {
    private Long id;
    private RequestStatus fromStatus;
    private RequestStatus toStatus;
    private String comment;
    private String actionBy;
    private LocalDateTime createdAt;
    private UserBasicInfoDto user;
}
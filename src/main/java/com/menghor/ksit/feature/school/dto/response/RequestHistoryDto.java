package com.menghor.ksit.feature.school.dto.response;

import com.menghor.ksit.enumations.RequestStatus;
import com.menghor.ksit.feature.auth.dto.resposne.UserBasicInfoDto;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class RequestHistoryDto {
    private Long id;
    private String title;
    private RequestStatus fromStatus;
    private RequestStatus toStatus;
    private String requestComment;
    private String staffComment;
    private String comment;
    private String actionBy;
    private LocalDateTime requestCreatedAt;
    private Long requestId;
    private UserBasicInfoDto user;
    private LocalDateTime createdAt;
}
package com.menghor.ksit.feature.school.dto.response;

import com.menghor.ksit.enumations.RequestStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RequestResponseDto {
    private Long id;
    private String title;
    private String description;
    private RequestStatus status;
    private String requestComment;
    private String staffComment;
    private UserBasicInfoDto user;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
package com.menghor.ksit.feature.school.dto.response;

import com.menghor.ksit.enumations.RequestStatus;
import com.menghor.ksit.feature.auth.dto.resposne.StudentResponseDto;
import com.menghor.ksit.feature.auth.dto.resposne.StudentUserResponseDto;
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
    private StudentUserResponseDto user;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
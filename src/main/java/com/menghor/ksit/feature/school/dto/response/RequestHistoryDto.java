package com.menghor.ksit.feature.school.dto.response;

import com.menghor.ksit.enumations.RequestStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RequestHistoryDto {

    private Long id;

    private RequestStatus fromStatus;

    private RequestStatus toStatus;

    private String comment;

    private String actionBy;

    private RequestResponseDto request;

    private UserBasicInfoDto user;

    private LocalDate createdAt;

    private LocalDate updatedAt;
}
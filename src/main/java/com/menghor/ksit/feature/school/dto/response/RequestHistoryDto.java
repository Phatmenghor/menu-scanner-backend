package com.menghor.ksit.feature.school.dto.response;

import com.menghor.ksit.enumations.RequestStatus;
import com.menghor.ksit.feature.auth.dto.resposne.UserBasicInfoDto;
import lombok.Data;

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
    private String actionBy; // Username for backward compatibility
    private LocalDateTime requestCreatedAt;
    private Long requestId;

    // The user who performed the action (staff/admin/student)
    private UserBasicInfoDto actionUser;

    // The user who originally made the request (from request.user)
    private UserBasicInfoDto requestOwner;

    private LocalDateTime createdAt;
}
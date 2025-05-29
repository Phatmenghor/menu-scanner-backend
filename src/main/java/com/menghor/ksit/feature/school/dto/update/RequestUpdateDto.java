package com.menghor.ksit.feature.school.dto.update;

import com.menghor.ksit.enumations.RequestStatus;
import lombok.Data;

@Data
public class RequestUpdateDto {
    private String title;
    private String description;
    private String requestComment;
    private RequestStatus status;
    private String staffComment;
}
package com.menghor.ksit.feature.school.dto.request;

import com.menghor.ksit.enumations.RequestStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RequestCreateDto {
    @NotBlank(message = "Title is required")
    private String title;
    private RequestStatus status;
    private String requestComment;


}

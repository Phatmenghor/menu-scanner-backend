package com.menghor.ksit.feature.school.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RequestCreateDto {
    @NotBlank(message = "Title is required")
    private String title;
    
    private String description;
    
    private String requestComment;
}

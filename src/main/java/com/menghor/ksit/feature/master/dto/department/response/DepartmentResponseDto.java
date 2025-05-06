package com.menghor.ksit.feature.master.dto.department.response;

import com.menghor.ksit.enumations.Status;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DepartmentResponseDto {
    private Long id;
    private String code;
    private String name;
    private String url_logo;
    private Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

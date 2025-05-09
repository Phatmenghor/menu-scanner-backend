package com.menghor.ksit.feature.master.dto.response;

import com.menghor.ksit.enumations.Status;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MajorResponseDto {
    private Long id;
    private String code;
    private String name;
    private Status status;
    private DepartmentResponseDto department;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

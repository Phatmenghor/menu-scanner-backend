package com.menghor.ksit.feature.master.dto.major.response;

import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.feature.master.dto.department.response.DepartmentResponseDto;
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

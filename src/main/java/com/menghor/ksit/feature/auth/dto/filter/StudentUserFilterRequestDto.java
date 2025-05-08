package com.menghor.ksit.feature.auth.dto.filter;

import com.menghor.ksit.enumations.Status;
import lombok.Data;

/**
 * Filter DTO for student users
 */
@Data
public class StudentUserFilterRequestDto {
    private String search;
    private Status status;
    private Long classId;
    private Integer academicYear;
    private Integer pageNo = 1;
    private Integer pageSize = 10;
}
package com.menghor.ksit.feature.school.dto.filter;

import com.menghor.ksit.enumations.Status;
import lombok.Data;

@Data
public class CourseFilterDto {
    private String search;
    private Long departmentId;
    private Status status;
    private Integer pageNo = 1;
    private Integer pageSize = 10;
}
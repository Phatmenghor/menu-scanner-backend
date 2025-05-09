package com.menghor.ksit.feature.course.dto.request;

import com.menghor.ksit.enumations.Status;
import lombok.Data;

@Data
public class CourseFilterDto {
    private String search;
    private Status status;
    private Integer pageNo = 1;
    private Integer pageSize = 10;
}

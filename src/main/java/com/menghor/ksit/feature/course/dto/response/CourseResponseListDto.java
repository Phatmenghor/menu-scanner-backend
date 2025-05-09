package com.menghor.ksit.feature.course.dto.response;

import lombok.Data;

@Data
public class CourseResponseListDto {
    private Long id;
    private String code;
    private String nameKH;
    private String nameEn;
    private Integer credit;
    private Integer theory;
    private Integer execute;
    private Integer apply;

    private String departmentName;
    private String subjectName;
    private String teacherName;
}

package com.menghor.ksit.feature.school.dto.update;

import com.menghor.ksit.enumations.Status;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CourseUpdateDto {
    private String code;
    private String nameKH;
    private String nameEn;
    private Integer credit;
    private Integer theory;
    private Integer execute;
    private Integer apply;
    private Integer totalHour;
    private String description;
    private String purpose;
    private String expectedOutcome;
    private Status status;
    private Long departmentId;
    private Long subjectId;
    private Long teacherId;
}
package com.menghor.ksit.feature.course.dto.request;

import com.menghor.ksit.enumations.Status;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CourseRequestDto {

    @NotBlank(message = "Course code is required")
    private String code;

    @NotBlank(message = "Course name khmer is required")
    private String nameKH;

    @NotBlank(message = "Course name english is required")
    private String nameEn;

    private Integer credit;
    private Integer theory;
    private Integer execute;
    private Integer apply;
    private Integer totalHour;
    private String description;
    private String purpose;
    private String expectedOutcome;
    private Status status = Status.ACTIVE;

    private Long departmentId;
    private Long subjectId;
    private Long teacherId;
}

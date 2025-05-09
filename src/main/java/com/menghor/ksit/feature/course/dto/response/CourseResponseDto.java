package com.menghor.ksit.feature.course.dto.response;

import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.feature.auth.dto.resposne.StaffUserResponseDto;
import com.menghor.ksit.feature.master.dto.department.response.DepartmentResponseDto;
import com.menghor.ksit.feature.master.dto.subject.response.SubjectResponseDto;
import lombok.Data;

@Data
public class CourseResponseDto {
    private Long id;
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

    private DepartmentResponseDto department;
    private SubjectResponseDto subject;
    private StaffUserResponseDto teacher;
}

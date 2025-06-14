package com.menghor.ksit.feature.survey.dto.filter;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.menghor.ksit.enumations.SemesterEnum;
import lombok.Data;

import java.time.LocalDate;

@Data
public class SurveyReportFilterDto {
    // Search by student name, ID, or course name
    private String search;
    
    // Filter by specific user (student)
    private Long userId;
    
    // Filter by specific schedule/course
    private Long scheduleId;
    
    // Filter by course
    private Long courseId;
    
    // Filter by class
    private Long classId;
    
    // Filter by teacher
    private Long teacherId;
    
    // Filter by department
    private Long departmentId;
    
    // Filter by major
    private Long majorId;
    
    // Filter by semester
    private SemesterEnum semester;
    
    // Filter by academic year
    private Integer academyYear;
    
    // Filter by submission date range
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    
    // Pagination (only for web preview)
    private Integer pageNo = 1;
    private Integer pageSize = 20;
}

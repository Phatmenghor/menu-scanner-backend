package com.menghor.ksit.feature.school.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class ScheduleBulkDuplicateResponseDto {
    private Long sourceClassId;
    private String sourceClassName;
    private Long sourceSemesterId;
    private String sourceSemesterName;
    private Integer sourceSemesterYear;
    
    private Long targetClassId;
    private String targetClassName;
    private Long targetSemesterId;
    private String targetSemesterName;
    private Integer targetSemesterYear;
    
    private Integer totalSourceSchedules;
    private Integer successfullyDuplicated;
    private Integer skipped; // Already exists
    private Integer failed;
    
    private List<ScheduleResponseDto> duplicatedSchedules;
    private List<String> errors;
    private String message;
}

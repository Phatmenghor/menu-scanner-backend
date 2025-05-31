package com.menghor.ksit.feature.attendance.dto.filter;

import com.menghor.ksit.enumations.SubmissionStatus;
import lombok.Data;

@Data
public class ScoreSessionFilterDto {
    private String search;
    private SubmissionStatus status;
    private Long teacherId;
    private Long scheduleId;
    private Long classId;
    private Long courseId;
    private Long studentId;
    private Integer pageNo = 1;
    private Integer pageSize = 10;
}
package com.menghor.ksit.feature.attendance.dto.response;

import com.menghor.ksit.enumations.SubmissionStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ScoreSessionResponseDto {
    private Long id;
    private Long scheduleId;
    private String classCode;
    private String courseName;
    private Long teacherId;
    private String teacherName;
    private String status;
    private String teacherComments;
    private String staffComments;
    private LocalDateTime submissionDate;
    private List<StudentScoreResponseDto> studentScores;
}
package com.menghor.ksit.feature.attendance.dto.response;

import com.menghor.ksit.enumations.SubmissionStatus;
import com.menghor.ksit.feature.master.dto.response.SemesterResponseDto;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ScoreSessionResponseDto {
    private Long id;
    private Long scheduleId;
    private String scheduleName;
    private Long teacherId;
    private String teacherName;
    private Long classId;
    private String className;
    private Long courseId;
    private String courseName;
    private SubmissionStatus status;
    private LocalDateTime submissionDate;
    private String teacherComments;
    private String staffComments;
    private List<StudentScoreResponseDto> studentScores;
    private LocalDateTime createdAt;
}
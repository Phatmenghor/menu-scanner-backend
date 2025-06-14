package com.menghor.ksit.feature.survey.dto.response;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
public class SurveyReportRowDto {
    // Student Information
    private Long responseId;
    private Long studentId;
    private String studentCode;
    private String studentNameEnglish;
    private String studentNameKhmer;
    private String studentEmail;
    private String studentPhone;
    private String className;
    private String majorName;
    private String departmentName;
    
    // Course/Schedule Information
    private Long scheduleId;
    private String courseCode;
    private String courseName;
    private String teacherName;
    private String roomName;
    private String dayOfWeek;
    private String timeSlot;
    private String semester;
    private Integer academyYear;
    
    // Survey Information
    private String surveyTitle;
    private LocalDateTime submittedAt;
    private String overallComment;
    private LocalDateTime createdAt;
    
    // Dynamic answers - flattened as direct properties for easy table display
    // Q1_Answer, Q2_Answer, etc. will be direct properties of this object
    private Map<String, Object> dynamicAnswers = new HashMap<>();
    
    // This annotation makes all dynamicAnswers appear as direct properties in JSON
    @JsonAnyGetter
    public Map<String, Object> getDynamicAnswers() {
        return dynamicAnswers;
    }
    
    // Helper method to add answers
    public void addAnswer(String questionKey, Object answerValue) {
        this.dynamicAnswers.put(questionKey, answerValue);
    }
}
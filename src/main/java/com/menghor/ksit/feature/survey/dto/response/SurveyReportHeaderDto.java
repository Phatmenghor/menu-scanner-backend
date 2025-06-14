package com.menghor.ksit.feature.survey.dto.response;

import lombok.Data;

@Data
public class SurveyReportHeaderDto {
    private String key;          // e.g., "studentCode", "Q1_Answer"
    private String label;        // e.g., "Student Code", "How would you rate...?"
    private String type;         // "TEXT", "NUMBER", "RATING", "DATE"
    private String category;     // "STUDENT", "COURSE", "SURVEY", "ANSWER"
    private Long questionId;     // For answer columns
    private Integer displayOrder;
}
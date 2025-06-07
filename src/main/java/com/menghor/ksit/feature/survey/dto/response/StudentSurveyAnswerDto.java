package com.menghor.ksit.feature.survey.dto.response;

import lombok.Data;

@Data
public class StudentSurveyAnswerDto {
    private Long id;
    private Long questionId;
    private String questionText;
    private String textAnswer;
    private Integer ratingAnswer;
}
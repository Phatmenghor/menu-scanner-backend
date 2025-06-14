package com.menghor.ksit.feature.survey.dto.response;

import com.menghor.ksit.enumations.QuestionTypeEnum;
import lombok.Data;

import java.util.List;

@Data
public class SurveyResponseQuestionDto {
    private Long questionId;
    private String questionText;
    private QuestionTypeEnum questionType;
    private Boolean required;
    private Integer displayOrder;
    private Integer minRating;
    private Integer maxRating;
    private String leftLabel;
    private String rightLabel;
    private List<RatingOptionDto> ratingOptions;
    
    // Student's answer for this question
    private String textAnswer;
    private Integer ratingAnswer;
    private Long answerId;
}
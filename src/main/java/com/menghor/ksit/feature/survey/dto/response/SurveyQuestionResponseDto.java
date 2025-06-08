package com.menghor.ksit.feature.survey.dto.response;

import com.menghor.ksit.enumations.QuestionTypeEnum;
import lombok.Data;

import java.util.List;

@Data
public class SurveyQuestionResponseDto {
    private Long id;
    private String questionText;
    private QuestionTypeEnum questionType;
    private Boolean required;
    private Integer displayOrder;
    private Integer minRating;
    private Integer maxRating;
    private String leftLabel;
    private String rightLabel;

    // For easy frontend looping
    private List<RatingOptionDto> ratingOptions;
}
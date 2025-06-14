package com.menghor.ksit.feature.survey.dto.response;

import com.menghor.ksit.enumations.QuestionTypeEnum;
import lombok.Data;

@Data
public class SurveyQuestionSnapshotDto {
    private Long id;
    private String questionText;
    private QuestionTypeEnum questionType;
    private Boolean required;
    private Integer displayOrder;
    private Integer minRating;
    private Integer maxRating;
    private String leftLabel;
    private String rightLabel;
}
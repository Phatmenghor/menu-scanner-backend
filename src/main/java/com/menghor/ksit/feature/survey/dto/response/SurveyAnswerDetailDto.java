package com.menghor.ksit.feature.survey.dto.response;

import com.menghor.ksit.enumations.QuestionTypeEnum;
import lombok.Data;

@Data
public class SurveyAnswerDetailDto {
    private Long answerId;
    private Long questionId;
    private String sectionTitle;
    private String questionText;
    private QuestionTypeEnum questionType;
    private String textAnswer;
    private Integer ratingAnswer;
    private Integer minRating;
    private Integer maxRating;
    private String leftLabel;
    private String rightLabel;
    private Integer displayOrder;
    private Integer sectionOrder;
    
    // Computed property for display
    public String getFormattedAnswer() {
        if (questionType == QuestionTypeEnum.RATING && ratingAnswer != null) {
            return ratingAnswer + "/5";
        } else if (questionType == QuestionTypeEnum.TEXT && textAnswer != null) {
            return textAnswer;
        }
        return "No answer provided";
    }
}
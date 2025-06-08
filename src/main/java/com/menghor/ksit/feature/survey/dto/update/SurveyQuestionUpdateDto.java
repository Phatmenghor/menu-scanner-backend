package com.menghor.ksit.feature.survey.dto.update;

import com.menghor.ksit.enumations.QuestionTypeEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SurveyQuestionUpdateDto {
    private Long id; // For updating existing questions

    @NotBlank(message = "Question text is required")
    private String questionText;

    @NotNull(message = "Question type is required")
    private QuestionTypeEnum questionType;

    private Boolean required = false;
    private Integer displayOrder = 0;

    // For RATING type questions
    private Integer minRating = 1;
    private Integer maxRating = 5;
    private String leftLabel = "Strongly Disagree";
    private String rightLabel = "Strongly Agree";
}
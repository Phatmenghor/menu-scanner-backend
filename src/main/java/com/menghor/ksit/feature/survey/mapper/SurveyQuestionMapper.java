package com.menghor.ksit.feature.survey.mapper;

import com.menghor.ksit.enumations.QuestionTypeEnum;
import com.menghor.ksit.feature.survey.dto.response.RatingOptionDto;
import com.menghor.ksit.feature.survey.dto.response.SurveyQuestionResponseDto;
import com.menghor.ksit.feature.survey.model.SurveyQuestionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring")
public interface SurveyQuestionMapper {

    @Mapping(target = "ratingOptions", source = ".", qualifiedByName = "generateRatingOptions")
    SurveyQuestionResponseDto toResponseDto(SurveyQuestionEntity entity);

    List<SurveyQuestionResponseDto> toResponseDtoList(List<SurveyQuestionEntity> entities);

    @Named("generateRatingOptions")
    default List<RatingOptionDto> generateRatingOptions(SurveyQuestionEntity entity) {
        List<RatingOptionDto> options = new ArrayList<>();

        // Only generate rating options for RATING type questions
        if (entity.getQuestionType() == QuestionTypeEnum.RATING) {
            int minRating = entity.getMinRating() != null ? entity.getMinRating() : 1;
            int maxRating = entity.getMaxRating() != null ? entity.getMaxRating() : 5;

            for (int i = minRating; i <= maxRating; i++) {
                String label = generateRatingLabel(i, minRating, maxRating,
                        entity.getLeftLabel(), entity.getRightLabel());
                options.add(new RatingOptionDto(i, label));
            }
        }

        return options;
    }

    default String generateRatingLabel(int currentValue, int minValue, int maxValue,
                                       String leftLabel, String rightLabel) {
        // For 5-point scale, provide meaningful labels
        if (maxValue == 5 && minValue == 1) {
            return switch (currentValue) {
                case 1 -> "1 - Poor";
                case 2 -> "2 - Fair";
                case 3 -> "3 - Good";
                case 4 -> "4 - Very Good";
                case 5 -> "5 - Excellent";
                default -> String.valueOf(currentValue);
            };
        }

        // For other scales or when custom labels are provided
        if (currentValue == minValue && leftLabel != null && !leftLabel.trim().isEmpty()) {
            return currentValue + " - " + leftLabel;
        } else if (currentValue == maxValue && rightLabel != null && !rightLabel.trim().isEmpty()) {
            return currentValue + " - " + rightLabel;
        } else {
            return String.valueOf(currentValue);
        }
    }
}
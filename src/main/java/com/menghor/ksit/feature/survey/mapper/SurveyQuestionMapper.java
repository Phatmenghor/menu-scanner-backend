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
        // If it's the minimum value and we have a left label
        if (currentValue == minValue && leftLabel != null && !leftLabel.trim().isEmpty()) {
            return String.format("(%d) %s", currentValue, leftLabel);
        }
        // If it's the maximum value and we have a right label
        else if (currentValue == maxValue && rightLabel != null && !rightLabel.trim().isEmpty()) {
            return String.format("(%d) %s", currentValue, rightLabel);
        }
        // For middle values or when no labels are provided
        else {
            return String.valueOf(currentValue);
        }
    }
}
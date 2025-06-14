package com.menghor.ksit.feature.survey.mapper;

import com.menghor.ksit.enumations.QuestionTypeEnum;
import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.enumations.StatusSurvey;
import com.menghor.ksit.feature.survey.dto.response.RatingOptionDto;
import com.menghor.ksit.feature.survey.dto.response.SurveyQuestionResponseDto;
import com.menghor.ksit.feature.survey.dto.update.SurveyQuestionUpdateDto;
import com.menghor.ksit.feature.survey.model.SurveyQuestionEntity;
import org.mapstruct.*;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring")
public interface SurveyQuestionMapper {

    // Response mapping
    @Mapping(target = "ratingOptions", source = ".", qualifiedByName = "generateRatingOptions")
    SurveyQuestionResponseDto toResponseDto(SurveyQuestionEntity entity);

    List<SurveyQuestionResponseDto> toResponseDtoList(List<SurveyQuestionEntity> entities);

    // Entity update mapping
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "section", ignore = true)
    @Mapping(target = "answers", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateQuestionFromDto(SurveyQuestionUpdateDto dto, @MappingTarget SurveyQuestionEntity entity);

    // Create new question
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "ACTIVE")
    @Mapping(target = "section", ignore = true)
    @Mapping(target = "answers", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "required", expression = "java(dto.getRequired() != null ? dto.getRequired() : false)")
    @Mapping(target = "minRating", expression = "java(dto.getMinRating() != null ? dto.getMinRating() : 1)")
    @Mapping(target = "maxRating", expression = "java(dto.getMaxRating() != null ? dto.getMaxRating() : 5)")
    SurveyQuestionEntity createQuestionFromDto(SurveyQuestionUpdateDto dto);

    // Helper methods for creating default questions
    default SurveyQuestionEntity createRatingQuestion(String questionText, int displayOrder) {
        SurveyQuestionEntity question = new SurveyQuestionEntity();
        question.setQuestionText(questionText);
        question.setQuestionType(QuestionTypeEnum.RATING);
        question.setRequired(true);
        question.setDisplayOrder(displayOrder);
        question.setMinRating(1);
        question.setMaxRating(5);
        question.setLeftLabel("Poor");
        question.setRightLabel("Excellent");
        question.setStatus(StatusSurvey.ACTIVE);
        return question;
    }

    default SurveyQuestionEntity createTextQuestion(String questionText, int displayOrder) {
        SurveyQuestionEntity question = new SurveyQuestionEntity();
        question.setQuestionText(questionText);
        question.setQuestionType(QuestionTypeEnum.TEXT);
        question.setRequired(false);
        question.setDisplayOrder(displayOrder);
        question.setStatus(StatusSurvey.ACTIVE);
        return question;
    }

    // Generate rating options with simple number labels
    @Named("generateRatingOptions")
    default List<RatingOptionDto> generateRatingOptions(SurveyQuestionEntity entity) {
        List<RatingOptionDto> options = new ArrayList<>();

        if (entity.getQuestionType() == QuestionTypeEnum.RATING) {
            int minRating = entity.getMinRating() != null ? entity.getMinRating() : 1;
            int maxRating = entity.getMaxRating() != null ? entity.getMaxRating() : 5;

            for (int i = minRating; i <= maxRating; i++) {
                // Simple rating labels - just numbers
                options.add(new RatingOptionDto(i, String.valueOf(i)));
            }
        }

        return options;
    }
}

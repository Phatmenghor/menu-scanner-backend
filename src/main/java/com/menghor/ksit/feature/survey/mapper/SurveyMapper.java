package com.menghor.ksit.feature.survey.mapper;

import com.menghor.ksit.enumations.QuestionTypeEnum;
import com.menghor.ksit.enumations.StatusSurvey;
import com.menghor.ksit.feature.survey.dto.request.SurveyResponseSubmitDto;
import com.menghor.ksit.feature.survey.dto.response.RatingOptionDto;
import com.menghor.ksit.feature.survey.dto.response.SurveyQuestionResponseDto;
import com.menghor.ksit.feature.survey.dto.response.SurveyResponseDto;
import com.menghor.ksit.feature.survey.dto.response.SurveySectionResponseDto;
import com.menghor.ksit.feature.survey.dto.update.SurveyUpdateDto;
import com.menghor.ksit.feature.survey.model.SurveyEntity;
import com.menghor.ksit.feature.survey.model.SurveyQuestionEntity;
import com.menghor.ksit.feature.survey.model.SurveyResponseEntity;
import org.mapstruct.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {SurveySectionMapper.class})
public interface SurveyMapper {

    // Response mapping - USE ACTIVE SECTIONS ONLY
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "sections", source = ".", qualifiedByName = "mapActiveSections")
    SurveyResponseDto toResponseDto(SurveyEntity entity);

    // Custom method to map only active sections
    @Named("mapActiveSections")
    default List<SurveySectionResponseDto> mapActiveSections(SurveyEntity entity) {
        if (entity == null || entity.getSections() == null) {
            return new ArrayList<>();
        }

        // Filter only ACTIVE sections and map them
        return entity.getSections().stream()
                .filter(section -> section.getStatus() == StatusSurvey.ACTIVE)
                .map(section -> {
                    SurveySectionResponseDto dto = new SurveySectionResponseDto();
                    dto.setId(section.getId());
                    dto.setTitle(section.getTitle());
                    dto.setDescription(section.getDescription());
                    dto.setDisplayOrder(section.getDisplayOrder());

                    // Map only active questions for this section
                    if (section.getQuestions() != null) {
                        List<SurveyQuestionResponseDto> activeQuestions = section.getQuestions().stream()
                                .filter(question -> question.getStatus() == StatusSurvey.ACTIVE)
                                .sorted((q1, q2) -> {
                                    int orderCompare = Integer.compare(
                                            q1.getDisplayOrder() != null ? q1.getDisplayOrder() : 0,
                                            q2.getDisplayOrder() != null ? q2.getDisplayOrder() : 0
                                    );
                                    return orderCompare != 0 ? orderCompare : Long.compare(q1.getId(), q2.getId());
                                })
                                .map(this::mapQuestionToDto)
                                .collect(Collectors.toList());
                        dto.setQuestions(activeQuestions);
                    }

                    return dto;
                })
                .sorted((s1, s2) -> {
                    int orderCompare = Integer.compare(
                            s1.getDisplayOrder() != null ? s1.getDisplayOrder() : 0,
                            s2.getDisplayOrder() != null ? s2.getDisplayOrder() : 0
                    );
                    return orderCompare != 0 ? orderCompare : Long.compare(s1.getId(), s2.getId());
                })
                .collect(Collectors.toList());
    }

    // Helper method to map question to DTO with simple rating labels
    default SurveyQuestionResponseDto mapQuestionToDto(SurveyQuestionEntity question) {
        SurveyQuestionResponseDto dto = new SurveyQuestionResponseDto();
        dto.setId(question.getId());
        dto.setQuestionText(question.getQuestionText());
        dto.setQuestionType(question.getQuestionType());
        dto.setRequired(question.getRequired());
        dto.setDisplayOrder(question.getDisplayOrder());
        dto.setMinRating(question.getMinRating());
        dto.setMaxRating(question.getMaxRating());
        dto.setLeftLabel(question.getLeftLabel());
        dto.setRightLabel(question.getRightLabel());

        // Generate rating options if it's a rating question
        if (question.getQuestionType() == QuestionTypeEnum.RATING) {
            List<RatingOptionDto> options = new ArrayList<>();
            int minRating = question.getMinRating() != null ? question.getMinRating() : 1;
            int maxRating = question.getMaxRating() != null ? question.getMaxRating() : 5;

            for (int i = minRating; i <= maxRating; i++) {
                // Simple rating labels - just numbers
                options.add(new RatingOptionDto(i, String.valueOf(i)));
            }
            dto.setRatingOptions(options);
        }

        return dto;
    }

    // Entity update mapping
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "schedule", ignore = true)
    @Mapping(target = "sections", ignore = true)
    @Mapping(target = "responses", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateSurveyFromDto(SurveyUpdateDto dto, @MappingTarget SurveyEntity entity);

    // Create default survey
    default SurveyEntity createDefaultSurvey() {
        SurveyEntity survey = new SurveyEntity();
        survey.setTitle("Student Course Evaluation Survey");
        survey.setDescription("Please provide your feedback about your learning experience in this course");
        survey.setStatus(StatusSurvey.ACTIVE);
        return survey;
    }

    // Create survey response entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "survey", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "schedule", ignore = true)
    @Mapping(target = "submittedAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "status", constant = "ACTIVE")
    @Mapping(target = "isCompleted", constant = "true")
    @Mapping(target = "answers", ignore = true)
    @Mapping(target = "surveySnapshot", ignore = true)
    @Mapping(target = "surveyTitleSnapshot", ignore = true)
    @Mapping(target = "surveyDescriptionSnapshot", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    SurveyResponseEntity createResponseFromDto(SurveyResponseSubmitDto dto);
}
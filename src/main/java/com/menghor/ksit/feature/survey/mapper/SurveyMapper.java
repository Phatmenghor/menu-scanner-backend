package com.menghor.ksit.feature.survey.mapper;

import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.feature.school.mapper.RequestMapper;
import com.menghor.ksit.feature.survey.dto.request.SurveyResponseSubmitDto;
import com.menghor.ksit.feature.survey.dto.response.SurveyResponseDto;
import com.menghor.ksit.feature.survey.dto.update.SurveyUpdateDto;
import com.menghor.ksit.feature.survey.model.SurveyEntity;
import com.menghor.ksit.feature.survey.model.SurveyResponseEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {SurveySectionMapper.class})
public interface SurveyMapper {

    // Response mapping
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "sections", source = "sections")
    @Mapping(target = "totalResponses", ignore = true)
    @Mapping(target = "hasUserResponded", ignore = true)
    SurveyResponseDto toResponseDto(SurveyEntity entity);

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
        survey.setStatus(Status.ACTIVE);
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
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    SurveyResponseEntity createResponseFromDto(SurveyResponseSubmitDto dto);
}
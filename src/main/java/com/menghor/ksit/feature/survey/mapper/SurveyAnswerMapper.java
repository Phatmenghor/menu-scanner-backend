package com.menghor.ksit.feature.survey.mapper;

import com.menghor.ksit.feature.survey.dto.request.SurveyAnswerSubmitDto;
import com.menghor.ksit.feature.survey.dto.response.StudentSurveyAnswerDto;
import com.menghor.ksit.feature.survey.model.SurveyAnswerEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SurveyAnswerMapper {

    // Response mapping
    @Mapping(target = "questionId", source = "question.id")
    @Mapping(target = "questionText", source = "question.questionText")
    StudentSurveyAnswerDto toStudentAnswerDto(SurveyAnswerEntity entity);

    // Entity creation mapping
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "response", ignore = true)
    @Mapping(target = "question", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    SurveyAnswerEntity createAnswerFromDto(SurveyAnswerSubmitDto dto);
}
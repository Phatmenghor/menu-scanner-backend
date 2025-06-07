package com.menghor.ksit.feature.survey.mapper;

import com.menghor.ksit.feature.survey.dto.response.StudentSurveyAnswerDto;
import com.menghor.ksit.feature.survey.model.SurveyAnswerEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SurveyAnswerMapper {
    
    @Mapping(target = "questionId", source = "question.id")
    @Mapping(target = "questionText", source = "question.questionText")
    StudentSurveyAnswerDto toStudentAnswerDto(SurveyAnswerEntity entity);
    
    List<StudentSurveyAnswerDto> toStudentAnswerDtoList(List<SurveyAnswerEntity> entities);
}
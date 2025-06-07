package com.menghor.ksit.feature.survey.mapper;

import com.menghor.ksit.feature.school.mapper.RequestMapper;
import com.menghor.ksit.feature.survey.dto.response.SurveyResponseDto;
import com.menghor.ksit.feature.survey.model.SurveyEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {RequestMapper.class, SurveySectionMapper.class})
public interface SurveyMapper {
    
    @Mapping(target = "createdBy", source = "createdBy")
    @Mapping(target = "sections", source = "sections")
    @Mapping(target = "totalResponses", ignore = true)
    @Mapping(target = "hasUserResponded", ignore = true)
    SurveyResponseDto toResponseDto(SurveyEntity entity);
}
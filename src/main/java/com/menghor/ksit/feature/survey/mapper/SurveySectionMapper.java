package com.menghor.ksit.feature.survey.mapper;

import com.menghor.ksit.feature.survey.dto.response.SurveySectionResponseDto;
import com.menghor.ksit.feature.survey.model.SurveySectionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {SurveyQuestionMapper.class})
public interface SurveySectionMapper {

    @Mapping(target = "questions", source = "questions")
    SurveySectionResponseDto toResponseDto(SurveySectionEntity entity);

    List<SurveySectionResponseDto> toResponseDtoList(List<SurveySectionEntity> entities);
}

package com.menghor.ksit.feature.survey.mapper;

import com.menghor.ksit.enumations.StatusSurvey;
import com.menghor.ksit.feature.survey.dto.response.SurveySectionResponseDto;
import com.menghor.ksit.feature.survey.dto.update.SurveySectionUpdateDto;
import com.menghor.ksit.feature.survey.model.SurveySectionEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses = {SurveyQuestionMapper.class})
public interface SurveySectionMapper {

    // Response mapping
    @Mapping(target = "questions", source = "questions")
    SurveySectionResponseDto toResponseDto(SurveySectionEntity entity);

    List<SurveySectionResponseDto> toResponseDtoList(List<SurveySectionEntity> entities);

    // Entity update mapping
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "survey", ignore = true)
    @Mapping(target = "questions", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateSectionFromDto(SurveySectionUpdateDto dto, @MappingTarget SurveySectionEntity entity);

    // Create new section
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "ACTIVE")
    @Mapping(target = "survey", ignore = true)
    @Mapping(target = "questions", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    SurveySectionEntity createSectionFromDto(SurveySectionUpdateDto dto);

    // Create section with defaults
    default SurveySectionEntity createSectionWithDefaults(String title, String description, int displayOrder) {
        SurveySectionEntity section = new SurveySectionEntity();
        section.setTitle(title);
        section.setDescription(description);
        section.setDisplayOrder(displayOrder);
        section.setStatus(StatusSurvey.ACTIVE);
        return section;
    }
}

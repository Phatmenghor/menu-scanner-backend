package com.menghor.ksit.feature.survey.mapper;

import com.menghor.ksit.feature.school.mapper.RequestMapper;
import com.menghor.ksit.feature.survey.dto.response.StudentSurveyResponseDto;
import com.menghor.ksit.feature.survey.model.SurveyResponseEntity;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", uses = {RequestMapper.class, SurveyAnswerMapper.class})
public interface SurveyResponseMapper {
    
    @Mapping(target = "surveyId", source = "survey.id")
    @Mapping(target = "user", source = "user")
    @Mapping(target = "answers", source = "answers")
    StudentSurveyResponseDto toStudentResponseDto(SurveyResponseEntity entity);
    
    List<StudentSurveyResponseDto> toStudentResponseDtoList(List<SurveyResponseEntity> entities);
    
    default CustomPaginationResponseDto<StudentSurveyResponseDto> toPaginationResponse(Page<SurveyResponseEntity> page) {
        List<StudentSurveyResponseDto> content = toStudentResponseDtoList(page.getContent());
        
        return CustomPaginationResponseDto.<StudentSurveyResponseDto>builder()
                .content(content)
                .pageNo(page.getNumber() + 1)
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}
package com.menghor.ksit.feature.attendance.mapper;

import com.menghor.ksit.feature.attendance.dto.response.StudentScoreResponseDto;
import com.menghor.ksit.feature.attendance.models.StudentScoreEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StudentScoreMapper {
    
    @Mapping(target = "studentId", source = "student.id")
    @Mapping(target = "studentName", expression = "java(entity.getStudent().getEnglishFirstName() + \" \" + entity.getStudent().getEnglishLastName())")
    @Mapping(target = "studentCode", source = "student.identifyNumber")
    @Mapping(target = "grade", expression = "java(entity.getGrade() != null ? entity.getGrade().getGrade() : null)")
    StudentScoreResponseDto toDto(StudentScoreEntity entity);
}
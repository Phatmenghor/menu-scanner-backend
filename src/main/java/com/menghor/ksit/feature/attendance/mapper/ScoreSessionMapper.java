package com.menghor.ksit.feature.attendance.mapper;

import com.menghor.ksit.feature.attendance.dto.response.ScoreSessionResponseDto;
import com.menghor.ksit.feature.attendance.dto.response.StudentScoreResponseDto;
import com.menghor.ksit.feature.attendance.models.ScoreSessionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public abstract class ScoreSessionMapper {
    
    @Autowired
    protected StudentScoreMapper studentScoreMapper;
    
    @Mapping(target = "scheduleId", source = "schedule.id")
    @Mapping(target = "className", source = "schedule.classes.code")
    @Mapping(target = "courseName", source = "schedule.course.nameEn")
    @Mapping(target = "teacherId", source = "teacher.id")
    @Mapping(target = "teacherName", expression = "java(entity.getTeacher().getEnglishFirstName() + \" \" + entity.getTeacher().getEnglishLastName())")
    @Mapping(target = "reviewerId", source = "reviewer.id")
    @Mapping(target = "reviewerName", expression = "java(entity.getReviewer() != null ? entity.getReviewer().getEnglishFirstName() + \" \" + entity.getReviewer().getEnglishLastName() : null)")
    @Mapping(target = "studentScores", expression = "java(mapStudentScores(entity))")
    public abstract ScoreSessionResponseDto toDto(ScoreSessionEntity entity);
    
    protected List<StudentScoreResponseDto> mapStudentScores(ScoreSessionEntity entity) {
        if (entity.getStudentScores() == null) {
            return null;
        }
        
        return entity.getStudentScores().stream()
                .map(studentScoreMapper::toDto)
                .collect(Collectors.toList());
    }
}
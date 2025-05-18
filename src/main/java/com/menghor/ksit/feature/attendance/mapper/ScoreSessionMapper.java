package com.menghor.ksit.feature.attendance.mapper;

import com.menghor.ksit.feature.attendance.dto.response.ScoreSessionResponseDto;
import com.menghor.ksit.feature.attendance.dto.response.StudentScoreResponseDto;
import com.menghor.ksit.feature.attendance.models.ScoreSessionEntity;
import com.menghor.ksit.feature.attendance.models.StudentScoreEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {StudentScoreMapper.class}
)
public interface ScoreSessionMapper {

    @Mapping(target = "scheduleId", source = "schedule.id")
    @Mapping(target = "className", source = "schedule.classes.code")
    @Mapping(target = "courseName", source = "schedule.course.nameEn")
    @Mapping(target = "teacherId", source = "teacher.id")
    @Mapping(target = "teacherName", expression = "java(entity.getTeacher().getEnglishFirstName() + \" \" + entity.getTeacher().getEnglishLastName())")
    @Mapping(target = "reviewerId", source = "reviewer.id")
    @Mapping(target = "reviewerName", expression = "java(entity.getReviewer() != null ? entity.getReviewer().getEnglishFirstName() + \" \" + entity.getReviewer().getEnglishLastName() : null)")
    @Mapping(target = "studentScores", expression = "java(mapStudentScores(entity))")
    ScoreSessionResponseDto toDto(ScoreSessionEntity entity);

    default List<StudentScoreResponseDto> mapStudentScores(ScoreSessionEntity entity) {
        if (entity.getStudentScores() == null) {
            return null;
        }

        return entity.getStudentScores().stream()
                .map(this::mapStudentScore)
                .collect(Collectors.toList());
    }

    default StudentScoreResponseDto mapStudentScore(StudentScoreEntity score) {
        StudentScoreResponseDto dto = new StudentScoreResponseDto();
        dto.setId(score.getId());
        dto.setStudentId(score.getStudent().getId());
        dto.setStudentName(score.getStudent().getEnglishFirstName() + " " + score.getStudent().getEnglishLastName());
        dto.setStudentCode(score.getStudent().getIdentifyNumber());
        dto.setAttendanceScore(score.getAttendanceScore());
        dto.setAssignmentScore(score.getAssignmentScore());
        dto.setMidtermScore(score.getMidtermScore());
        dto.setFinalScore(score.getFinalScore());
        dto.setTotalScore(score.getTotalScore());
        dto.setGrade(score.getGrade() != null ? score.getGrade().getGrade() : null);
        dto.setComments(score.getComments());
        return dto;
    }
}
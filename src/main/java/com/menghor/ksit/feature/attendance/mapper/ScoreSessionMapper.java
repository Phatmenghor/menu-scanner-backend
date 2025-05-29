package com.menghor.ksit.feature.attendance.mapper;

import com.menghor.ksit.feature.attendance.dto.response.ScoreSessionResponseDto;
import com.menghor.ksit.feature.attendance.dto.response.StudentScoreResponseDto;
import com.menghor.ksit.feature.attendance.models.ScoreSessionEntity;
import com.menghor.ksit.feature.attendance.models.StudentScoreEntity;
import com.menghor.ksit.feature.auth.models.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
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
    @Mapping(target = "classCode", source = "schedule.classes.code")
    @Mapping(target = "courseName", source = "schedule.course.nameEn")
    @Mapping(target = "teacherId", source = "teacher.id")
    @Mapping(source = "teacher", target = "teacherName", qualifiedByName = "mapTeacherName")
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

    // Custom method to handle the teacher name logic
    @Named("mapTeacherName")
    default String mapTeacherName(UserEntity teacher) {
        if (teacher == null) {
            return null;
        }

        // Check if English names are available and not null
        if (teacher.getEnglishFirstName() != null && teacher.getEnglishLastName() != null) {
            return teacher.getEnglishFirstName() + " " + teacher.getEnglishLastName();
        }

        // Fallback to Khmer names
        if (teacher.getKhmerFirstName() != null && teacher.getKhmerLastName() != null) {
            return teacher.getKhmerFirstName() + " " + teacher.getKhmerLastName();
        }

        // If all names are null, return username or null
        return teacher.getUsername();
    }

    // Helper method to map student name with null checks (similar to mapTeacherName)
    default String mapStudentName(UserEntity student) {
        if (student == null) {
            return null;
        }

        // Check if English names are available and not null
        if (student.getEnglishFirstName() != null && student.getEnglishLastName() != null) {
            return student.getEnglishFirstName() + " " + student.getEnglishLastName();
        }

        // Fallback to Khmer names
        if (student.getKhmerFirstName() != null && student.getKhmerLastName() != null) {
            return student.getKhmerFirstName() + " " + student.getKhmerLastName();
        }

        // If all names are null, return username or identify number
        return student.getUsername() != null ? student.getUsername() : student.getIdentifyNumber();
    }

    default StudentScoreResponseDto mapStudentScore(StudentScoreEntity score) {
        StudentScoreResponseDto dto = new StudentScoreResponseDto();
        dto.setId(score.getId());
        dto.setStudentId(score.getStudent().getId());
        dto.setStudentName(mapStudentName(score.getStudent())); // Use the helper method
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
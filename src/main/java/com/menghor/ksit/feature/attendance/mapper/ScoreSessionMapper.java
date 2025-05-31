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
import org.mapstruct.factory.Mappers;

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
    @Mapping(target = "status", expression = "java(entity.getStatus() != null ? entity.getStatus().name() : null)")
    @Mapping(target = "studentScores", source = "studentScores", qualifiedByName = "mapAndSortStudentScores")
    ScoreSessionResponseDto toDto(ScoreSessionEntity entity);

    // Inject the StudentScoreMapper
    StudentScoreMapper studentScoreMapper = Mappers.getMapper(StudentScoreMapper.class);

    /**
     * Maps and sorts student scores by student name for consistent ordering
     */
    @Named("mapAndSortStudentScores")
    default List<StudentScoreResponseDto> mapAndSortStudentScores(List<StudentScoreEntity> studentScores) {
        if (studentScores == null || studentScores.isEmpty()) {
            return null;
        }

        return studentScores.stream()
                .map(studentScoreMapper::toDto)
                .sorted((s1, s2) -> {
                    // Sort by student name (English first, then Khmer fallback)
                    String name1 = s1.getStudentNameEnglish() != null ? s1.getStudentNameEnglish() : s1.getStudentNameKhmer();
                    String name2 = s2.getStudentNameEnglish() != null ? s2.getStudentNameEnglish() : s2.getStudentNameKhmer();

                    if (name1 == null && name2 == null) return 0;
                    if (name1 == null) return 1;
                    if (name2 == null) return -1;

                    return name1.compareToIgnoreCase(name2);
                })
                .collect(Collectors.toList());
    }

    /**
     * Maps English name with proper null handling
     */
    @Named("mapEnglishName")
    default String mapEnglishName(UserEntity student) {
        if (student == null) {
            return null;
        }

        String firstName = student.getEnglishFirstName();
        String lastName = student.getEnglishLastName();

        if (firstName != null && lastName != null) {
            return firstName.trim() + " " + lastName.trim();
        } else if (firstName != null) {
            return firstName.trim();
        } else if (lastName != null) {
            return lastName.trim();
        }

        return null;
    }

    /**
     * Maps Khmer name with proper null handling
     */
    @Named("mapKhmerName")
    default String mapKhmerName(UserEntity student) {
        if (student == null) {
            return null;
        }

        String firstName = student.getKhmerFirstName();
        String lastName = student.getKhmerLastName();

        if (firstName != null && lastName != null) {
            return firstName.trim() + " " + lastName.trim();
        } else if (firstName != null) {
            return firstName.trim();
        } else if (lastName != null) {
            return lastName.trim();
        }

        return null;
    }

    /**
     * Enhanced teacher name mapping with comprehensive fallback logic
     */
    @Named("mapTeacherName")
    default String mapTeacherName(UserEntity teacher) {
        if (teacher == null) {
            return null;
        }

        // Priority 1: English names
        String englishName = mapEnglishName(teacher);
        if (englishName != null && !englishName.trim().isEmpty()) {
            return englishName;
        }

        // Priority 2: Khmer names
        String khmerName = mapKhmerName(teacher);
        if (khmerName != null && !khmerName.trim().isEmpty()) {
            return khmerName;
        }

        // Priority 3: Username
        if (teacher.getUsername() != null && !teacher.getUsername().trim().isEmpty()) {
            return teacher.getUsername();
        }

        // Priority 4: Email (if different from username)
        if (teacher.getEmail() != null && !teacher.getEmail().trim().isEmpty()
                && !teacher.getEmail().equals(teacher.getUsername())) {
            return teacher.getEmail();
        }

        // Fallback: Return a default identifier
        return "Teacher #" + teacher.getId();
    }


}
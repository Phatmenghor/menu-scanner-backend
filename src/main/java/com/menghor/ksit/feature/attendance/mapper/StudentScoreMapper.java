package com.menghor.ksit.feature.attendance.mapper;

import com.menghor.ksit.feature.attendance.dto.response.StudentScoreResponseDto;
import com.menghor.ksit.feature.attendance.models.StudentScoreEntity;
import com.menghor.ksit.feature.auth.models.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface StudentScoreMapper {

    @Mapping(target = "studentIdentityNumber", source = "student.identifyNumber")
    @Mapping(target = "studentId", source = "student.id")
    @Mapping(target = "gender", source = "student.gender")
    @Mapping(target = "dateOfBirth", source = "student.dateOfBirth")
    @Mapping(target = "studentNameEnglish", source = "student", qualifiedByName = "mapEnglishStudentName")
    @Mapping(target = "studentNameKhmer", source = "student", qualifiedByName = "mapKhmerStudentName")
    @Mapping(target = "totalScore", expression = "java(calculateTotalScore(entity))")
    @Mapping(target = "grade", expression = "java(mapGradeLevel(entity))")
    @Mapping(target = "attendanceScore", source = "attendanceScore")
    @Mapping(target = "assignmentScore", source = "assignmentScore")
    @Mapping(target = "midtermScore", source = "midtermScore")
    @Mapping(target = "finalScore", source = "finalScore")
    @Mapping(target = "comments", source = "comments")
    StudentScoreResponseDto toDto(StudentScoreEntity entity);

    /**
     * Maps English student name with proper concatenation and null handling
     */
    @Named("mapEnglishStudentName")
    default String mapEnglishStudentName(UserEntity student) {
        if (student == null) {
            return null;
        }

        String firstName = student.getEnglishFirstName();
        String lastName = student.getEnglishLastName();

        // Handle various combinations of first and last names
        if (firstName != null && lastName != null) {
            firstName = firstName.trim();
            lastName = lastName.trim();
            if (!firstName.isEmpty() && !lastName.isEmpty()) {
                return firstName + " " + lastName;
            } else if (!firstName.isEmpty()) {
                return firstName;
            } else if (!lastName.isEmpty()) {
                return lastName;
            }
        } else if (firstName != null) {
            firstName = firstName.trim();
            if (!firstName.isEmpty()) {
                return firstName;
            }
        } else if (lastName != null) {
            lastName = lastName.trim();
            if (!lastName.isEmpty()) {
                return lastName;
            }
        }

        return null;
    }

    /**
     * Maps Khmer student name with proper concatenation and null handling
     */
    @Named("mapKhmerStudentName")
    default String mapKhmerStudentName(UserEntity student) {
        if (student == null) {
            return null;
        }

        String firstName = student.getKhmerFirstName();
        String lastName = student.getKhmerLastName();

        // Handle various combinations of first and last names
        if (firstName != null && lastName != null) {
            firstName = firstName.trim();
            lastName = lastName.trim();
            if (!firstName.isEmpty() && !lastName.isEmpty()) {
                return firstName + " " + lastName;
            } else if (!firstName.isEmpty()) {
                return firstName;
            } else if (!lastName.isEmpty()) {
                return lastName;
            }
        } else if (firstName != null) {
            firstName = firstName.trim();
            if (!firstName.isEmpty()) {
                return firstName;
            }
        } else if (lastName != null) {
            lastName = lastName.trim();
            if (!lastName.isEmpty()) {
                return lastName;
            }
        }

        return null;
    }

    /**
     * Calculates total score with null safety and proper validation
     */
    default Double calculateTotalScore(StudentScoreEntity entity) {
        if (entity == null) {
            return 0.0;
        }

        // Use the entity's built-in method but add validation
        Double totalScore = entity.getTotalScore();

        // Additional validation to ensure score doesn't exceed 100
        if (totalScore != null && totalScore > 100.0) {
            // Log warning or handle overflow case
            return 100.0;
        }

        return totalScore != null ? totalScore : 0.0;
    }

    /**
     * Maps grade level with null safety
     */
    default String mapGradeLevel(StudentScoreEntity entity) {
        if (entity == null) {
            return null;
        }

        try {
            var gradeLevel = entity.getGrade();
            return gradeLevel != null ? gradeLevel.getGrade() : "N/A";
        } catch (Exception e) {
            // Handle any potential calculation errors
            return "ERROR";
        }
    }
}
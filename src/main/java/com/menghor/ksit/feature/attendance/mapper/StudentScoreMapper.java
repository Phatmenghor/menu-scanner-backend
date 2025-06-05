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
    @Mapping(target = "attendanceScore", expression = "java(calculateAttendanceScore(entity))")
    @Mapping(target = "assignmentScore", expression = "java(calculateAssignmentScore(entity))")
    @Mapping(target = "midtermScore", expression = "java(calculateMidtermScore(entity))")
    @Mapping(target = "finalScore", expression = "java(calculateFinalScore(entity))")
    @Mapping(target = "attendanceRawScore", expression = "java(mapRawScore(entity.getAttendanceRawScore()))")
    @Mapping(target = "assignmentRawScore", expression = "java(mapRawScore(entity.getAssignmentRawScore()))")
    @Mapping(target = "midtermRawScore", expression = "java(mapRawScore(entity.getMidtermRawScore()))")
    @Mapping(target = "finalRawScore", expression = "java(mapRawScore(entity.getFinalRawScore()))")
    @Mapping(target = "comments", source = "comments")
    StudentScoreResponseDto toDto(StudentScoreEntity entity);

    @Named("mapEnglishStudentName")
    default String mapEnglishStudentName(UserEntity student) {
        if (student == null) {
            return null;
        }

        String firstName = student.getEnglishFirstName();
        String lastName = student.getEnglishLastName();

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

    @Named("mapKhmerStudentName")
    default String mapKhmerStudentName(UserEntity student) {
        if (student == null) {
            return null;
        }

        String firstName = student.getKhmerFirstName();
        String lastName = student.getKhmerLastName();

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

    default Double calculateTotalScore(StudentScoreEntity entity) {
        if (entity == null) {
            return 0.0;
        }

        try {
            Double totalScore = entity.getTotalScoreDouble();
            if (totalScore != null && totalScore > 100.0) {
                return 100.0;
            }
            return totalScore != null ? totalScore : 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }

    default Double calculateAttendanceScore(StudentScoreEntity entity) {
        if (entity == null) {
            return 0.0;
        }
        try {
            return entity.getAttendanceScoreDouble();
        } catch (Exception e) {
            return 0.0;
        }
    }

    default Double calculateAssignmentScore(StudentScoreEntity entity) {
        if (entity == null) {
            return 0.0;
        }
        try {
            return entity.getAssignmentScoreDouble();
        } catch (Exception e) {
            return 0.0;
        }
    }

    default Double calculateMidtermScore(StudentScoreEntity entity) {
        if (entity == null) {
            return 0.0;
        }
        try {
            return entity.getMidtermScoreDouble();
        } catch (Exception e) {
            return 0.0;
        }
    }

    default Double calculateFinalScore(StudentScoreEntity entity) {
        if (entity == null) {
            return 0.0;
        }
        try {
            return entity.getFinalScoreDouble();
        } catch (Exception e) {
            return 0.0;
        }
    }

    default Double mapRawScore(java.math.BigDecimal rawScore) {
        if (rawScore == null) {
            return 0.0;
        }
        return rawScore.doubleValue();
    }

    default String mapGradeLevel(StudentScoreEntity entity) {
        if (entity == null) {
            return null;
        }
        try {
            var gradeLevel = entity.getGrade();
            return gradeLevel != null ? gradeLevel.getGrade() : "N/A";
        } catch (Exception e) {
            return "ERROR";
        }
    }
}
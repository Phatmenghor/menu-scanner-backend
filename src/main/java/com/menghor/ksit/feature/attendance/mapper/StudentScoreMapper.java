package com.menghor.ksit.feature.attendance.mapper;

import com.menghor.ksit.feature.attendance.dto.response.StudentScoreResponseDto;
import com.menghor.ksit.feature.attendance.models.StudentScoreEntity;
import com.menghor.ksit.feature.auth.models.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StudentScoreMapper {

    @Mapping(target = "studentId", source = "student.id")
    @Mapping(target = "studentName", expression = "java(formatStudentName(entity.getStudent()))")
    @Mapping(target = "studentCode", source = "student.identifyNumber")
    @Mapping(target = "totalScore", expression = "java(entity.getTotalScore())")
    @Mapping(target = "grade", expression = "java(entity.getGrade() != null ? entity.getGrade().getGrade() : null)")
    StudentScoreResponseDto toDto(StudentScoreEntity entity);

    default String formatStudentName(UserEntity student) {
        String firstName = student.getEnglishFirstName() != null ? student.getEnglishFirstName() : "";
        String lastName = student.getEnglishLastName() != null ? student.getEnglishLastName() : "";

        if (firstName.isEmpty() && lastName.isEmpty()) {
            return student.getUsername() != null ? student.getUsername() : "Unknown Student";
        }

        return (firstName + " " + lastName).trim();
    }
}
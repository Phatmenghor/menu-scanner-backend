package com.menghor.ksit.feature.attendance.mapper;

import com.menghor.ksit.feature.attendance.dto.response.StudentScoreResponseDto;
import com.menghor.ksit.feature.attendance.models.StudentScoreEntity;
import com.menghor.ksit.feature.auth.models.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface StudentScoreMapper {

    StudentScoreMapper INSTANCE = Mappers.getMapper(StudentScoreMapper.class);

    @Mapping(source = "student.id", target = "studentId")
    @Mapping(source = "student", target = "studentNameKhmer", qualifiedByName = "mapStudentKhmerName")
    @Mapping(source = "student", target = "studentNameEnglish", qualifiedByName = "mapStudentEnglishName")
    @Mapping(source = "student.identifyNumber", target = "studentIdentityNumber")
    @Mapping(source = "student.gender", target = "gender")
    @Mapping(source = "student.dateOfBirth", target = "dateOfBirth")
    StudentScoreResponseDto toDto(StudentScoreEntity entity);

    @Named("mapStudentKhmerName")
    default String mapStudentKhmerName(UserEntity student) {
        if (student == null) {
            return null;
        }

        if (student.getKhmerFirstName() != null && student.getKhmerLastName() != null) {
            return student.getKhmerFirstName() + " " + student.getKhmerLastName();
        }

        return null;
    }

    @Named("mapStudentEnglishName")
    default String mapStudentEnglishName(UserEntity student) {
        if (student == null) {
            return null;
        }

        if (student.getEnglishFirstName() != null && student.getEnglishLastName() != null) {
            return student.getEnglishFirstName() + " " + student.getEnglishLastName();
        }

        if (student.getKhmerFirstName() != null && student.getKhmerLastName() != null) {
            return student.getKhmerFirstName() + " " + student.getKhmerLastName();
        }

        return null;
    }

}
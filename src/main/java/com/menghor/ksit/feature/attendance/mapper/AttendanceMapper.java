package com.menghor.ksit.feature.attendance.mapper;

import com.menghor.ksit.feature.attendance.dto.response.AttendanceDto;
import com.menghor.ksit.feature.attendance.dto.response.AttendanceSessionDto;
import com.menghor.ksit.feature.attendance.models.AttendanceEntity;
import com.menghor.ksit.feature.attendance.models.AttendanceSessionEntity;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface AttendanceMapper {
    AttendanceMapper INSTANCE = Mappers.getMapper(AttendanceMapper.class);
    
    @Mapping(source = "student.id", target = "studentId")
    @Mapping(source = "student", target = "studentName")  // Pass the whole student object
    @Mapping(source = "student.identifyNumber", target = "identifyNumber")
    @Mapping(source = "attendanceSession.id", target = "attendanceSessionId")
    AttendanceDto toDto(AttendanceEntity entity);
    
    @Mapping(source = "schedule.id", target = "scheduleId")
    @Mapping(source = "schedule.room.name", target = "roomName")
    @Mapping(source = "schedule.classes.code", target = "classCode")
    @Mapping(source = "teacher.id", target = "teacherId")
    @Mapping(source = "teacher", target = "teacherName")
    @Mapping(source = "finalizationStatus", target = "finalizationStatus")
    AttendanceSessionDto toDto(AttendanceSessionEntity entity);

    // Custom method to handle the name logic
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

        // If all names are null, return username or null
        return student.getUsername();
    }
}
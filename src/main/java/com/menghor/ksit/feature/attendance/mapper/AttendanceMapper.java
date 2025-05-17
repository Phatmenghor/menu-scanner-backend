package com.menghor.ksit.feature.attendance.mapper;

import com.menghor.ksit.feature.attendance.dto.response.AttendanceDto;
import com.menghor.ksit.feature.attendance.dto.response.AttendanceSessionDto;
import com.menghor.ksit.feature.attendance.models.AttendanceEntity;
import com.menghor.ksit.feature.attendance.models.AttendanceSessionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface AttendanceMapper {
    
    AttendanceMapper INSTANCE = Mappers.getMapper(AttendanceMapper.class);
    
    @Mapping(source = "student.id", target = "studentId")
    @Mapping(source = "student.name", target = "studentName")
    @Mapping(source = "student.code", target = "studentCode")
    @Mapping(source = "attendanceSession.id", target = "attendanceSessionId")
    AttendanceDto toDto(AttendanceEntity entity);
    
    @Mapping(source = "studentId", target = "student.id")
    @Mapping(target = "student.name", ignore = true)
    @Mapping(target = "student.code", ignore = true)
    @Mapping(source = "attendanceSessionId", target = "attendanceSession.id")
    AttendanceEntity toEntity(AttendanceDto dto);
    
    @Mapping(source = "schedule.id", target = "scheduleId")
    @Mapping(source = "schedule.course.name", target = "courseName")
    @Mapping(source = "schedule.room.name", target = "roomName")
    @Mapping(source = "schedule.classes.code", target = "className")
    @Mapping(source = "teacher.id", target = "teacherId")
    @Mapping(source = "teacher.name", target = "teacherName")
    AttendanceSessionDto toDto(AttendanceSessionEntity entity);
    
    @Mapping(source = "scheduleId", target = "schedule.id")
    @Mapping(target = "schedule.course", ignore = true)
    @Mapping(target = "schedule.room", ignore = true)
    @Mapping(target = "schedule.classes", ignore = true)
    @Mapping(source = "teacherId", target = "teacher.id")
    @Mapping(target = "teacher.name", ignore = true)
    @Mapping(target = "attendances", ignore = true)
    AttendanceSessionEntity toEntity(AttendanceSessionDto dto);
}

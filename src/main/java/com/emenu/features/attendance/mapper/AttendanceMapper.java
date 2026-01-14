package com.emenu.features.attendance.mapper;

import com.emenu.features.attendance.dto.response.AttendanceResponse;
import com.emenu.features.attendance.models.Attendance;
import org.springframework.stereotype.Component;

@Component
public class AttendanceMapper {

    public AttendanceResponse toResponse(Attendance attendance) {
        return AttendanceResponse.builder()
                .id(attendance.getId())
                .userId(attendance.getUser().getId())
                .userName(attendance.getUser().getFirstName() + " " + attendance.getUser().getLastName())
                .workScheduleId(attendance.getWorkSchedule().getId())
                .scheduleName(attendance.getWorkSchedule().getScheduleName())
                .attendanceDate(attendance.getAttendanceDate())
                .checkInTime(attendance.getCheckInTime())
                .checkInLatitude(attendance.getCheckInLatitude())
                .checkInLongitude(attendance.getCheckInLongitude())
                .checkInAddress(attendance.getCheckInAddress())
                .checkInNote(attendance.getCheckInNote())
                .checkOutTime(attendance.getCheckOutTime())
                .checkOutLatitude(attendance.getCheckOutLatitude())
                .checkOutLongitude(attendance.getCheckOutLongitude())
                .checkOutAddress(attendance.getCheckOutAddress())
                .checkOutNote(attendance.getCheckOutNote())
                .totalWorkMinutes(attendance.getTotalWorkMinutes())
                .lateMinutes(attendance.getLateMinutes())
                .status(attendance.getStatus())
                .remarks(attendance.getRemarks())
                .createdAt(attendance.getCreatedAt())
                .updatedAt(attendance.getUpdatedAt())
                .build();
    }
}

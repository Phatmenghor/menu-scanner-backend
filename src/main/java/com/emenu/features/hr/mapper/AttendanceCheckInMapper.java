package com.emenu.features.hr.mapper;

import com.emenu.features.hr.dto.response.AttendanceCheckInResponse;
import com.emenu.features.hr.models.AttendanceCheckIn;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AttendanceCheckInMapper {
    AttendanceCheckInResponse toResponse(AttendanceCheckIn checkIn);
    List<AttendanceCheckInResponse> toResponseList(List<AttendanceCheckIn> checkIns);
}

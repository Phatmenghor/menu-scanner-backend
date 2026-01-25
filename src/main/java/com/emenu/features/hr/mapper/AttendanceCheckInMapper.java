package com.emenu.features.hr.mapper;

import com.emenu.features.hr.dto.helper.AttendanceCheckInCreateHelper;
import com.emenu.features.hr.dto.response.AttendanceCheckInResponse;
import com.emenu.features.hr.models.AttendanceCheckIn;
import com.emenu.shared.mapper.PaginationMapper;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", uses = {PaginationMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AttendanceCheckInMapper {
    AttendanceCheckInResponse toResponse(AttendanceCheckIn checkIn);

    List<AttendanceCheckInResponse> toResponseList(List<AttendanceCheckIn> checkIns);

    /**
     * Create AttendanceCheckIn from helper DTO - pure MapStruct mapping
     */
    AttendanceCheckIn createFromHelper(AttendanceCheckInCreateHelper helper);
}

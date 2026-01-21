package com.emenu.features.hr.mapper;

import com.emenu.features.auth.dto.response.BusinessResponse;
import com.emenu.features.auth.models.Business;
import com.emenu.features.hr.dto.response.AttendanceCheckInResponse;
import com.emenu.features.hr.models.AttendanceCheckIn;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.mapper.PaginationMapper;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", uses = {PaginationMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AttendanceCheckInMapper {
    AttendanceCheckInResponse toResponse(AttendanceCheckIn checkIn);

    List<AttendanceCheckInResponse> toResponseList(List<AttendanceCheckIn> checkIns);
}

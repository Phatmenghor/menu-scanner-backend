package com.emenu.features.hr.mapper;

import com.emenu.features.hr.dto.request.AttendanceCheckInRequest;
import com.emenu.features.hr.dto.response.AttendanceResponse;
import com.emenu.features.hr.dto.update.AttendanceUpdateRequest;
import com.emenu.features.hr.models.Attendance;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses = {AttendanceCheckInMapper.class})
public interface AttendanceMapper {
    
    @Mapping(target = "checkIns", source = "checkIns")
    @Mapping(target = "statusEnumName", ignore = true)
    AttendanceResponse toResponse(Attendance attendance);
    
    List<AttendanceResponse> toResponseList(List<Attendance> attendances);
    
    Attendance toEntity(AttendanceCheckInRequest request);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "statusEnumId", ignore = true)
    void updateEntity(AttendanceUpdateRequest request, @MappingTarget Attendance attendance);
}
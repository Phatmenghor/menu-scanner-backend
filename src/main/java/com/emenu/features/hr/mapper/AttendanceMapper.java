package com.emenu.features.hr.mapper;

import com.emenu.features.hr.dto.response.AttendanceResponse;
import com.emenu.features.hr.dto.update.AttendanceUpdateRequest;
import com.emenu.features.hr.models.Attendance;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", uses = {AttendanceCheckInMapper.class})
public interface AttendanceMapper {
    AttendanceResponse toResponse(Attendance attendance);
    List<AttendanceResponse> toResponseList(List<Attendance> attendances);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(AttendanceUpdateRequest request, @MappingTarget Attendance attendance);
}